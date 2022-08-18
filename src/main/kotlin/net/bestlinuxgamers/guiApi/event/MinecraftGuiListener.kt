package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Klasse zum Empfangen von Gui-Spezifischen Events
 * @param dispatcher Handler, an welchen die Events weitergeleitet werden
 */
@OptIn(EventDispatcherOnly::class)
class MinecraftGuiListener(private val dispatcher: MinecraftGuiEventDispatcher) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST) //TODO eins high (fürs canceln) und eins Monitor für endgültiges Resultat
    fun onInventoryClick(event: InventoryClickEvent) {
        dispatcher.dispatchClickEvent(event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClose(event: InventoryCloseEvent) {
        dispatcher.dispatchCloseEvent(event)
    }

    //TODO InventoryInteractEvent
}
