package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Klasse repräsentiert einen Adapter für den [MinecraftGuiEventHandler].
 * Jeder Adapter empfängt ein [Event] durch das Bukkit Event-System und
 * führt damit die durch den [MinecraftGuiEventHandler] verteilten [EventRegistration]s durch.
 * @param T Typ des Events, welches der Adapter empfängt und weiterleitet
 */
abstract class EventListenerAdapter<T : Event> : Listener {

    private val registrations: MutableSet<EventRegistration<out EventListenerAdapter<T>, T>> = mutableSetOf()

    /**
     * Fügt eine [EventRegistration] hinzu.
     * Dadurch wird bei einem erfolgreich, durch den [EventRegistration.identifier], identifizierten Event
     * die dazugehörige [EventRegistration.action] ausgeführt.
     * @param registration Identifier eines Events und Aktion,
     * welche im Falle einer erfolgreichen Identifizierung ausgeführt wird.
     * Der Typ der Registration muss dieser Klasse entsprechen.
     * @throws IllegalArgumentException Wenn die [registration] nur für einen anderen Adapter benutzt werden kann.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun addRegistration(registration: EventRegistration<out EventListenerAdapter<out Event>, out Event>) {
        if (registration.adapterClass != this::class)
            throw IllegalArgumentException("The provided EventRegistration is only registerable for the ${registration.adapterClass.simpleName} Adapter!")
        registrations.add(registration as EventRegistration<out EventListenerAdapter<T>, T>)
    }

    /**
     * Entfernt eine bereits registrierte [EventRegistration].
     * @param registration Registration, die entfernt werden soll
     * @see addRegistration
     */
    internal fun removeRegistration(registration: EventRegistration<out EventListenerAdapter<out Event>, out Event>) =
        registrations.remove(registration)

    /**
     * Wendet das Event auf alle registrierten [EventRegistration]s an.
     * Dafür wird es zuerst für eine [EventRegistration] anhand ihres [EventIdentifier]s identifiziert.
     * Sollte dies positiv verlaufen wird die jeweilige [EventAction] ausgeführt.
     * @param event Event, welches weitergeleitet werden soll
     * @see addRegistration
     */
    @EventDispatcherOnly
    internal fun dispatchEvent(event: T) {
        registrations.forEach { if (it.identifier.isEvent(event)) it.action.runAction(event) }
    }

    /**
     * Listener Methode, welche die Schnittstelle zum Bukkit Event-System darstellt.
     * Hier werden events initial empfangen.
     * Implementation der Methode muss mit der @[EventHandler] Annotation annotiert werden.
     * @param event Event, welches vom Bukkit Event-System übergeben wird
     */
    @EventHandler
    abstract fun onEvent(event: T)

}

/**
 * [EventListenerAdapter] für [InventoryClickEvent]s
 */
class ClickEventListenerAdapter : EventListenerAdapter<InventoryClickEvent>() {
    @EventHandler(priority = EventPriority.HIGHEST)
    @OptIn(EventDispatcherOnly::class)
    override fun onEvent(event: InventoryClickEvent) {
        dispatchEvent(event)
    }

}

/**
 * [EventListenerAdapter] für [InventoryCloseEvent]s
 */
class CloseEventListenerAdapter : EventListenerAdapter<InventoryCloseEvent>() {
    @EventHandler(priority = EventPriority.MONITOR)
    @OptIn(EventDispatcherOnly::class)
    override fun onEvent(event: InventoryCloseEvent) {
        dispatchEvent(event)
    }
}

/**
 * [EventListenerAdapter] für [PlayerQuitEvent]s
 */
class QuitEventListenerAdapter : EventListenerAdapter<PlayerQuitEvent>() {
    @EventHandler(priority = EventPriority.MONITOR)
    @OptIn(EventDispatcherOnly::class)
    override fun onEvent(event: PlayerQuitEvent) {
        dispatchEvent(event)
    }
}
