package net.bestlinuxgamers.guiApi.endpoint

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurfaceInterface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

/**
 * Die oberste GUI Komponente, welche zum rendern der untergeordneten Komponenten und
 * dem Event-Empfang/Weiterleiten zuständig ist.
 * @param surface Grafische Oberfläche
 * @param schedulerProvider Klasse zum registrieren von Minecraft schedulern
 * @param tickSpeed Die Schnelligkeit der GUI render Updates in Minecraft Ticks
 * @param static Ob die Komponente nur initial gerendert werden soll ([GuiComponent.static])
 * @param background Items für Slots, auf denen keine Komponente liegt ([GuiComponent.renderFallback])
 * @see GuiComponent
 */
@OptIn(SurfaceManagerOnly::class)
abstract class ComponentEndpoint(
    private val surface: GuiSurfaceInterface,
    private val schedulerProvider: SchedulerProvider,
    private val tickSpeed: Long = 20,
    static: Boolean = false,
    background: ItemStack? = null
) : GuiComponent(surface.generateReserved(), static, background) {

    private var frameCount: Long = 0
    private var scheduler: BukkitTask? = null

    init {
        super.lock()
        surface.registerEndpoint(this) //TODO evtl. erst in open
    }

    /**
     * Öffnet das Inventar für den Spieler und startet alle Animationen
     */
    @Suppress("unused")
    fun open() {
        if (surface.isOpened()) return

        surface.open(renderNext())
        startUpdateScheduler()
    }

    /**
     * Schließt das Inventar für den Spieler
     * @see GuiSurfaceInterface.close
     */
    @Suppress("unused")
    fun close() = surface.close()

    //Event

    /**
     * Führt die Schließ-Routine durch.
     * Sollte nur von einem close Event eines EventDispatchers aufgerufen werden!
     * @see [net.bestlinuxgamers.guiApi.event.SurfaceEventDispatcher.dispatchCloseEvent]
     */
    @EventDispatcherOnly
    internal fun performClose() {
        stopUpdateScheduler()
        surface.onClose()
        surface.unregisterEndpoint()
    }

    /**
     * Führt die Klick-Routine durch.
     * Sollte nur von einem click Event eines EventDispatchers aufgerufen werden!
     * @see [net.bestlinuxgamers.guiApi.event.SurfaceEventDispatcher.dispatchClickEvent]
     */
    @EventDispatcherOnly
    internal fun performClick(clickedSlot: Int, event: InventoryClickEvent) {
        if (clickedSlot < 0 || clickedSlot >= reservedSlots.totalReserved) return
        event.isCancelled = true //TODO feinere einstellung
        click(event, clickedSlot)
    }

    //TODO clickRequest

    //render

    /**
     * Rendert das nächste Bild
     * @see [renderNextFrame]
     */
    private fun renderNext() = renderNextFrame(frameCount++)

    //scheduler

    /**
     * Startet den update Tick scheduler
     */
    private fun startUpdateScheduler() {
        if ((scheduler != null) || super.static) return

        scheduler = schedulerProvider.runTaskTimerAsynchronously(tickSpeed, tickSpeed) { performUpdateTick() }
    }

    /**
     * Stoppt den update Tick scheduler
     * @see startUpdateScheduler
     */
    private fun stopUpdateScheduler() {
        if (!static) {
            scheduler?.cancel().also { scheduler = null }
        }
    }

    /**
     * Führt den nächsten update Tick aus
     */
    private fun performUpdateTick() {
        val lastRender = super.getLastRender()
        surface.updateItems(renderNext(), lastRender)
        //TODO was, wenn render länger, als tickSpeed benötigt //TODO Items sync updaten
    }

}
