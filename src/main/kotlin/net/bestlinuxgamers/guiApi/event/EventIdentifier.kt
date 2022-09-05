package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

/**
 * Schnittstelle zum zuordnen eines Minecraft Events zu einem dem dazugehörigen [net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay]
 */
interface EventIdentifier {

    /**
     * @param event Event, welches auf eine Zugehörigkeit geprüft werden soll
     * @return Ob das angegebene Event zu dem Display gehört
     */
    fun isClickEvent(event: InventoryClickEvent): Boolean

    /**
     * @param event Event, welches auf eine Zugehörigkeit geprüft werden soll
     * @return Ob das angegebene Event zu dem Display gehört
     */
    fun isCloseEvent(event: InventoryCloseEvent): Boolean
}

/**
 * Ordnet ein Event mithilfe eines Inventars zu
 * @param inventory Inventar, auf das im Event geprüft wird
 */
open class InventoryEventIdentifier(private val inventory: Inventory) : EventIdentifier {

    override fun isClickEvent(event: InventoryClickEvent): Boolean = event.clickedInventory == inventory

    override fun isCloseEvent(event: InventoryCloseEvent): Boolean = event.inventory == inventory
}

/**
 * Ordnet ein Event mithilfe mehrerer anderer [EventIdentifier] zu.
 * Dabei gilt es als zugeordnet, wenn eines der angegebenen [EventIdentifier] true zurückgibt.
 * @param identifiers [EventIdentifier], die zum prüfen genutzt werden
 */
class MergedEventIdentifier(private val identifiers: Set<EventIdentifier>) : EventIdentifier {

    override fun isClickEvent(event: InventoryClickEvent): Boolean = identifiers.any { it.isClickEvent(event) }

    override fun isCloseEvent(event: InventoryCloseEvent): Boolean = identifiers.any { it.isCloseEvent(event) }
}
