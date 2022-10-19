package net.bestlinuxgamers.guiApi.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import kotlin.reflect.KClass

/**
 * Klasse zum Identifizieren von Events.
 * @param adapterClass Klasse des Adapters [T].
 * Diese wird zum zuordnen einer Instanz dieser Klasse zu dem dazugehörigen [EventListenerAdapter] benötigt.
 * @param T Adapter, dessen Events identifiziert werden sollen
 * @param E Event-Typ, der identifiziert werden soll
 */
abstract class EventIdentifier<T : EventListenerAdapter<E>, E : Event>(val adapterClass: KClass<T>) { //TODO Adapter verschieben nach EventRegistration
    /**
     * Identifiziert ein Event
     * @param event Event, welches identifiziert werden soll
     * @return Ob das Event zugehörig ist
     */
    abstract fun isEvent(event: E): Boolean
}

// merged

/**
 * Identifiziert ein Event anhand von mehreren anderen [EventIdentifier].
 * Events werden als zugehörig bewertet, sobald eines der angegeben [EventIdentifier.isEvent] true zurückgibt.
 * @param identifiers Alle Identifier, die vereint werden sollen
 * @param T Typ des Adapters, dessen [EventIdentifier] vereint werden sollen
 * @param E Typ des Events, den die [identifiers] identifizieren müssen
 */
class MergedIdentifiersIdentifier<T : EventListenerAdapter<E>, E : Event>(private val identifiers: Set<EventIdentifier<T, E>>) :
    EventIdentifier<T, E>(identifiers.first().adapterClass) {
    override fun isEvent(event: E): Boolean = identifiers.any { it.isEvent(event) }
}

//click

/**
 * [EventIdentifier] für [InventoryClickEvent]s
 */
abstract class ClickEventIdentifier :
    EventIdentifier<ClickEventListenerAdapter, InventoryClickEvent>(ClickEventListenerAdapter::class)

/**
 * Klasse zum identifizieren, ob ein Event einer Gui Aktion zugehörig ist.
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
abstract class CloseEventIdentifier :
    EventIdentifier<CloseEventListenerAdapter, InventoryCloseEvent>(CloseEventListenerAdapter::class)

/**
 * Klasse zum identifizieren, ob ein Event einer Gui Aktion zugehörig ist.
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
abstract class QuitEventIdentifier :
    EventIdentifier<QuitEventListenerAdapter, PlayerQuitEvent>(QuitEventListenerAdapter::class)

/**
 * Klasse zum identifizieren, ob ein Event einer Gui Aktion zugehörig ist.
 * @param player Spieler, auf den geprüft wird
 */
class QuitEventPlayerIdentifier(private val player: Player) : QuitEventIdentifier() {
    override fun isEvent(event: PlayerQuitEvent): Boolean = event.player == player
}

