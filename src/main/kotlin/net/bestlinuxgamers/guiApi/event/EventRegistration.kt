package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.reflect.KClass

/**
 * Eine registrierung für den [MinecraftGuiEventHandler].
 * Dieser verbindet einen [EventIdentifier] mit einer [EventAction].
 * Dies sind alle Informationen, welche benötigt werden, um das Event-System zu nutzen.
 * @param identifier Klasse zum Identifizieren, ob ein Event zu der [action] zugehörig ist
 * @param action Aktion, welche bei einem Event ausgeführt wird
 * @param T Typ des [EventListenerAdapter]s, auf den sich die Registrierung bezieht
 * @param E Typ des [Event]s, auf den sich die Registrierung bezieht
 */
open class EventRegistration<T : EventListenerAdapter<E>, E : Event>(
    val identifier: EventIdentifier<E>,
    val action: EventAction<E>,
    val adapterClass: KClass<T>
)

/**
 * [EventRegistration] für [InventoryClickEvent]s
 */
class ClickEventRegistration(
    identifier: EventIdentifier<InventoryClickEvent>,
    action: EventAction<InventoryClickEvent>,
) : EventRegistration<ClickEventListenerAdapter, InventoryClickEvent>(
    identifier, action, ClickEventListenerAdapter::class
)

/**
 * [EventRegistration] für [InventoryCloseEvent]s
 */
class CloseEventRegistration(
    identifier: EventIdentifier<InventoryCloseEvent>,
    action: EventAction<InventoryCloseEvent>,
) : EventRegistration<CloseEventListenerAdapter, InventoryCloseEvent>(
    identifier, action, CloseEventListenerAdapter::class
)

/**
 * [EventRegistration] für [PlayerQuitEvent]s
 */
class QuitEventRegistration(
    identifier: EventIdentifier<PlayerQuitEvent>,
    action: EventAction<PlayerQuitEvent>,
) : EventRegistration<QuitEventListenerAdapter, PlayerQuitEvent>(
    identifier, action, QuitEventListenerAdapter::class
)

