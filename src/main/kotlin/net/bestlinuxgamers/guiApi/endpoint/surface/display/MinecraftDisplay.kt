package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.event.EventIdentifier
import net.bestlinuxgamers.guiApi.event.EventListenerAdapter
import net.bestlinuxgamers.guiApi.event.EventRegistration
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Eine grafische Oberfläche in Minecraft
 */
interface MinecraftDisplay : DisplayInterface {

    val player: Player

    val clickEventIdentifier: EventIdentifier<InventoryClickEvent>
    val eventRegistrations: Set<EventRegistration<out EventListenerAdapter<out Event>, out Event>>

    /**
     * Generiert eine [EventRegistration], welche bei einem Event, welches das Display
     * schließen soll, die [action] ausführt.
     * @param action Aktion, die bei dem Event ausgeführt wird
     */
    fun generateCloseActionRegistration(action: () -> Unit): EventRegistration<out EventListenerAdapter<out Event>, out Event>

    companion object {
        const val INVENTORY_WIDTH = 9
    }
}
