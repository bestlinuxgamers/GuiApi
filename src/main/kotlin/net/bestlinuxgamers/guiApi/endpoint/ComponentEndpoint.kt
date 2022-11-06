package net.bestlinuxgamers.guiApi.endpoint

import net.bestlinuxgamers.guiApi.component.CallDispatcherOnly
import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.RenderOnly
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
 * @param renderTick Ob das Gui im Intervall von [tickSpeed] erneut gerendert werden soll
 * @param tickSpeed Die Schnelligkeit der GUI render Updates von [renderTick] in Minecraft Ticks
 * @param onDemandRender Ob das manuelle Auslösen des Rendervorgangs durch eine Komponente erlaubt sein soll
 * @param static Ob die Komponente nur initial gerendert werden soll ([GuiComponent.static])
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen ([GuiComponent.smartRender])
 * @param background Items für Slots, auf denen keine Komponente liegt ([GuiComponent.renderFallback])
 * @see GuiComponent
 */
@OptIn(SurfaceManagerOnly::class)
abstract class ComponentEndpoint(
    private val surface: GuiSurfaceInterface,
    private val schedulerProvider: SchedulerProvider, //TODO nullable
    private val renderTick: Boolean = true,
    private val tickSpeed: Long = 20,
    private val onDemandRender: Boolean = true,
    static: Boolean = false,
    smartRender: Boolean = true,
    background: ItemStack? = null
) : GuiComponent(surface.generateReserved(), static, smartRender, background) {

    private var frameCount: Long = 0
    private var scheduler: BukkitTask? = null
    private var tickCount: Long = 0

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

        @OptIn(RenderOnly::class)
        surface.open(renderNext())
        startUpdateScheduler()
    }

    /**
     * Schließt das Inventar für den Spieler
     * @see GuiSurfaceInterface.close
     */
    @Suppress("unused")
    fun close() {
        @OptIn(EventDispatcherOnly::class)
        performClose()
        surface.close()
    }

    //Event

    /**
     * Führt die Schließ-Routine durch.
     * Sollte nur durch ein Event eines EventListeners aufgerufen werden!
     */
    @EventDispatcherOnly
    internal fun performClose() {
        stopUpdateScheduler()
        surface.onClose()
        surface.unregisterEndpoint()
    }

    /**
     * Führt die Klick-Routine durch.
     * Sollte nur durch ein klick Event eines EventListeners aufgerufen werden!
     * @see click
     */
    @EventDispatcherOnly
    internal fun performClick(clickedSlot: Int, event: InventoryClickEvent) {
        if (clickedSlot < 0 || clickedSlot >= reservedSlots.totalReserved) return
        event.isCancelled = true //TODO feinere Einstellung
        click(event, clickedSlot)
    }

    //TODO clickRequest

    //user-render-interaction

    /**
     * Empfängt als rendernde Komponente den Befehl einen Rendervorgang zu starten
     * und startet diesen, wenn [onDemandRender] aktiviert ist.
     */
    override fun triggerReRender() {
        if (!onDemandRender) return

        @OptIn(RenderOnly::class)
        performSurfaceUpdate()
    }

    //render

    /**
     * Rendert das nächste Bild und sendet vorher den [beforeRender] call.
     * @see [renderNextFrame]
     */
    @RenderOnly
    private fun renderNext(): Array<ItemStack?> {
        val frame = frameCount++
        @OptIn(CallDispatcherOnly::class)
        dispatchBeforeRender(frame)
        return renderNextFrame(frame)
    }

    /**
     * Rendert das nächste Bild und schreibt dieses in das [surface]
     * @see renderNext
     */
    @RenderOnly
    private fun performSurfaceUpdate() {
        val lastRender = getLastRender()
        surface.updateItems(renderNext(), lastRender)
        //TODO was, wenn render länger, als tickSpeed benötigt //TODO Items sync updaten
    }

    //scheduler

    /**
     * Startet den update Tick scheduler
     */
    private fun startUpdateScheduler() {
        if (!renderTick || super.static || scheduler != null) return

        scheduler = schedulerProvider.runTaskTimerAsynchronously(tickSpeed, tickSpeed) {
            onRenderTick(tickCount++, frameCount)
            @OptIn(RenderOnly::class)
            performSurfaceUpdate()
        }
    }

    /**
     * Stoppt den update Tick scheduler
     * @see startUpdateScheduler
     */
    private fun stopUpdateScheduler() {
        if (!static && renderTick) {
            scheduler?.cancel().also { scheduler = null }
        }
    }

}
