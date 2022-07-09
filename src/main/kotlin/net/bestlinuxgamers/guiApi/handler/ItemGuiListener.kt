package net.bestlinuxgamers.guiApi.handler

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Klasse zum Empfangen von Gui-Spezifischen Events
 * @param handler Handler, an welchen die Events weitergeleitet werden
 */
class ItemGuiListener(private val handler: ItemGuiHandler) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        handler.dispatchClickEvent(event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClose(event: InventoryCloseEvent) {
        handler.dispatchCloseEvent(event)
    }
}
