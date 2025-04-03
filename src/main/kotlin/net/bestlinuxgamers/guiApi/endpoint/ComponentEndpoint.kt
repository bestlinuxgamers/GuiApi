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
 * Die oberste GUI Komponente, welche zum Rendern der untergeordneten Komponenten und
 * dem Event-Empfang/Weiterleiten zuständig ist.
 * @param surface Grafische Oberfläche
 * @param schedulerProvider Klasse zum Registrieren von Minecraft schedulern.
 * Wenn null, ist [componentTick] automatisch deaktiviert.
 * Außerdem können Komponenten nur noch durch [directOnDemandRender] verändert werden.
 * @param componentTick [GuiComponent.componentTick]
 * @param tickSpeed [GuiComponent.tickSpeed]
 * @param directOnDemandRender Ob das neu rendern außerhalb eines Ticks durch
 * @param autoRender Ob das GUI bei einer erkannten Änderung automatisch aktualisiert werden soll.
 * @param autoRenderSpeed In wie vielen Ticks das GUI bei einer erkannten Änderung spätestens aktualisiert werden soll.
 * @param static [GuiComponent.static]
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen ([GuiComponent.smartRender])
 * @param background Items für Slots, auf denen keine Komponente liegt ([GuiComponent.renderFallback])
 * @see GuiComponent
 */
@OptIn(SurfaceManagerOnly::class)
abstract class ComponentEndpoint(
    private val surface: GuiSurfaceInterface,
    private val schedulerProvider: SchedulerProvider?,
    componentTick: Boolean = true,
    tickSpeed: Long = 20,
    private val directOnDemandRender: Boolean = false,
    static: Boolean = false,
    autoRender: Boolean = true,
    autoRenderSpeed: Int = 1,
    smartRender: Boolean = true,
    background: ItemStack? = null
) : GuiComponent(
    surface.generateReserved(),
    static,
    autoRender,
    autoRenderSpeed,
    smartRender,
    background,
    componentTick && schedulerProvider != null,
    tickSpeed
) {
    private var frameCount: Long = 1
    private var scheduler: BukkitTask? = null
    private var tickCount: Long = 0
    private var requestedRenderIn: Int? = null

    init {
        super.lock()
        surface.registerEndpoint(this) //TODO evtl. erst in open
    }

    /**
     * Öffnet das Inventar für den Spieler und startet alle Animationen
     */
    fun open() {
        if (surface.isOpened()) return
        if (tickCount <= 0) {
            startTickScheduler()
            @OptIn(RenderOnly::class)
            surface.open(renderNext())
        } else {
            surface.open(getLastRender()!!)
            startTickScheduler()
        }
    }

    /**
     * Schließt das Inventar für den Spieler
     * @see GuiSurfaceInterface.close
     */
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
     * Empfängt als rendernde Komponente den Befehl einen Rendervorgang zu starten.
     * @param renderIn Die Zeit in Ticks, in welcher der Rendervorgang spätestens gestartet werden soll.
     * Wenn 0 angegeben ist, wird der Rendervorgang direkt gestartet.
     * Dies ist allerdings nur möglich, wenn [directOnDemandRender] aktiviert ist.
     */
    @CallDispatcherOnly
    override fun passUpTriggerReRender(renderIn: Int) {
        if (directOnDemandRender && renderIn <= 0) {
            @OptIn(RenderOnly::class)
            performSurfaceUpdate()
        } else {
            setRequestedReRender(renderIn)
        }
    }

    /**
     * Setzt die Zeit, in welcher ein neuer Rendervorgang gestartet wird.
     * @param renderIn Zeit, in welcher spätestens ein Rendervorgang gestartet werden soll.
     * Dabei wird beachtet, ob bereits vorher ein Rendervorgang gestartet werden soll.
     */
    private fun setRequestedReRender(renderIn: Int) {
        val renderInSanitized = if (renderIn > 0) renderIn else 1

        if (requestedRenderIn?.let { renderInSanitized < it } != false) requestedRenderIn = renderInSanitized
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
    private fun performSurfaceUpdate() { //TODO evtl. smartRender Referenzen entfernen
        if (static || (smartRender && !hasUnRenderedChanges())) return

        val lastRender = getLastRender()
        val rendered = renderNext()

        requestedRenderIn = null

        if (!smartRender && rendered.contentEquals(lastRender)) return
        surface.updateItems(rendered, lastRender)
        //TODO was, wenn render länger, als tickSpeed benötigt //TODO Items sync updaten
    }

    //scheduler

    /**
     * Startet den component Tick scheduler
     */
    private fun startTickScheduler() {
        if (super.static || scheduler != null) return

        scheduler = schedulerProvider?.runTaskTimerAsynchronously(0, GLOBAL_TICK_SPEED) {
            val tick = tickCount++
            @OptIn(CallDispatcherOnly::class)
            dispatchOnComponentTick(tick, frameCount)

            if (tick >= 1 && // Tick 0 will be rendered by the open() function
                requestedRenderIn?.let { requestedRenderIn = it - 1; it - 1 <= 0 } == true
            ) {
                @OptIn(RenderOnly::class)
                performSurfaceUpdate()
            }
        }
    }

    /**
     * Stoppt den update Tick scheduler
     * @see startTickScheduler
     */
    private fun stopUpdateScheduler() {
        if (!static) {
            scheduler?.cancel().also { scheduler = null }
        }
    }

    companion object {
        /**
         * Die globale Tick-geschwindigkeit als Abstand zwischen Minecraft-Ticks.
         * Kleinster Wert ist 1 (für jeden Minecraft-Tick).
         */
        private const val GLOBAL_TICK_SPEED: Long = 1
    }
}
