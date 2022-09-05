package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Struktur einer Klasse, welche Events weiterleitet
 */
interface SurfaceEventDispatcher {

    /**
     * Leitet ein click-Event weiter
     * @param event Event, welches weitergeleitet werden soll
     */
    @EventDispatcherOnly
    fun dispatchClickEvent(event: InventoryClickEvent)

    /**
     * Leitet ein close-Event weiter
     * @param event Event, welches weitergeleitet werden soll
     */
    @EventDispatcherOnly
    fun dispatchCloseEvent(event: InventoryCloseEvent)
}
