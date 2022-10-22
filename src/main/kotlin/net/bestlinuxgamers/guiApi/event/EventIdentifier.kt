package net.bestlinuxgamers.guiApi.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory

/**
 * Klasse zum Identifizieren von Events.
 * Diese wird zum Zuordnen einer Instanz dieser Klasse zu dem dazugehörigen [EventListenerAdapter] benötigt.
 * @param E Event-Typ, der identifiziert werden soll
 */
abstract class EventIdentifier<E : Event> {
    /**
     * Identifiziert ein Event
     * @param event Event, welches identifiziert werden soll
     * @return Ob das Event zugehörig ist
     */
    abstract fun isEvent(event: E): Boolean
}

//lambda

class LambdaEventIdentifier<E : Event>(private val identify: (event: E) -> Boolean) : EventIdentifier<E>() {
    override fun isEvent(event: E): Boolean = identify(event)
}

// merged

/**
 * Identifiziert ein Event anhand von mehreren anderen [EventIdentifier].
 * Events werden als zugehörig bewertet, sobald eines der angegeben [EventIdentifier.isEvent] true zurückgibt.
 * @param identifiers Alle Identifier, die vereint werden sollen
 * @param E Typ des Events, den die [identifiers] identifizieren müssen
 */
class MergedIdentifiersIdentifier<E : Event>(private val identifiers: Set<EventIdentifier<E>>) : EventIdentifier<E>() {
    override fun isEvent(event: E): Boolean = identifiers.any { it.isEvent(event) }
}

//click

/**
 * [EventIdentifier] für [InventoryClickEvent]s
 */
abstract class ClickEventIdentifier : EventIdentifier<InventoryClickEvent>()

/**
 * Klasse zum Identifizieren, ob ein Event einer Gui Aktion zugehörig ist.
 * @param player Spieler, auf den geprüft wird
 * @param inventory Inventar, welches benutzt werden soll
 */
class GuiClickEventIdentifier(private val player: Player, private val inventory: Inventory) :
    ClickEventIdentifier() {
    override fun isEvent(event: InventoryClickEvent): Boolean =
        event.whoClicked == player && event.clickedInventory == inventory && event.slot >= 0
}

//close

/**
 * [EventIdentifier] für [InventoryCloseEvent]s
 */
abstract class CloseEventIdentifier : EventIdentifier<InventoryCloseEvent>()

/**
 * Klasse zum Identifizieren, ob ein Event einer Gui Aktion zugehörig ist.
 * @param player Spieler, auf den geprüft wird
 * @param inventory Inventar, welches benutzt werden soll
 */
class GuiCloseEventIdentifier(private val player: Player, private val inventory: Inventory) :
    CloseEventIdentifier() {
    override fun isEvent(event: InventoryCloseEvent): Boolean = event.player == player && event.inventory == inventory
}

//quit

/**
 * [EventIdentifier] für [PlayerQuitEvent]s
 */
abstract class QuitEventIdentifier : EventIdentifier<PlayerQuitEvent>()

/**
 * Klasse zum Identifizieren, ob ein Event einer Gui Aktion zugehörig ist.
 * @param player Spieler, auf den geprüft wird
 */
class QuitEventPlayerIdentifier(private val player: Player) : QuitEventIdentifier() {
    override fun isEvent(event: PlayerQuitEvent): Boolean = event.player == player
}

