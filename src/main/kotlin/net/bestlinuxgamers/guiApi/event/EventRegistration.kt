package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.Event

/**
 * Eine registrierung für den [MinecraftGuiEventHandler].
 * Dieser verbindet einen [EventIdentifier] mit einer [EventAction].
 * Dies sind alle Informationen, welche benötigt werden, um das Event-System zu nutzen.
 * @param identifier Klasse zum Identifizieren, ob ein Event zu der [action] zugehörig ist
 * @param action Aktion, welche bei einem Event ausgeführt wird
 * @param T Typ des [EventListenerAdapter]s, auf den sich die Registrierung bezieht
 * @param E Typ des [Event]s, auf den sich die Registrierung bezieht
 */
open class EventRegistration<T : EventListenerAdapter<E>, E : Event>( //TOD Adapter Klasse hier speichern
    val identifier: EventIdentifier<T, E>,
    val action: EventAction<E>
)
