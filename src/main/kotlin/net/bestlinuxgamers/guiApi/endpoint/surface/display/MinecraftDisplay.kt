package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.event.EventIdentifier
import net.bestlinuxgamers.guiApi.event.EventListenerAdapter
import net.bestlinuxgamers.guiApi.event.EventRegistration
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Eine grafische Oberfläche in Minecraft
 */
interface MinecraftDisplay : DisplayInterface {

    val player: Player

    val clickEventIdentifier: EventIdentifier<InventoryClickEvent>
    val closeActionEventIdentifier: EventIdentifier<InventoryCloseEvent> //TODO generischer Typ, da unterschiedliche Events möglich
    val eventRegistrations: Set<EventRegistration<out EventListenerAdapter<out Event>, out Event>>

    companion object {
        const val INVENTORY_WIDTH = 9
    }
}
