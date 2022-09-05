package net.bestlinuxgamers.guiApi.endpoint.surface.util

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.SurfaceEventDispatcher
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Klasse zum empfangen von Events und der Weiterleitung an ein [ComponentEndpoint]
 * @param display Oberfläche, dem das Event zugehörig ist.
 * @param endpoint Endpoint, an den das Event weitergeleitet werden soll
 */
class SurfaceEventReceiver(
    private val display: MinecraftDisplay,
    private val endpoint: ComponentEndpoint
) : SurfaceEventDispatcher {

    /**
     * Leitet das Event an das [ComponentEndpoint] weiter.
     * Dabei wird der Komponenten Slot anhand des [display] berechnet und übergeben.
     * @param event Event, welches weitergeleitet wird
     * @see ComponentEndpoint.performClick
     * @see MinecraftDisplay.getComponentSlot
     */
    @EventDispatcherOnly
    override fun dispatchClickEvent(event: InventoryClickEvent) {
        endpoint.performClick(display.getComponentSlot(event), event)
    }

    /**
     * Leitet das Event an das [ComponentEndpoint] weiter
     * @param event Event, welches weitergeleitet wird
     * @see ComponentEndpoint.performClose
     */
    @EventDispatcherOnly
    override fun dispatchCloseEvent(event: InventoryCloseEvent) {
        endpoint.performClose()
    }

}
