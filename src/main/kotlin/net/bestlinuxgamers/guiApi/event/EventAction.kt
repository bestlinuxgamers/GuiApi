package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.Event

/**
 * Repräsentiert eine Aktion, die bei einem Event ausgeführt wird.
 * @param T Typ des Events
 */
abstract class EventAction<T : Event> {
    /**
     * Führt die Event-Aktion aus
     * @param event Event, welches die Aktion ausgelöst hat
     */
    @EventDispatcherOnly
    abstract fun runAction(event: T)
}

/**
 * Eine [EventAction], welche eine lambda Funktion als Aktion ausführt.
 * @param action Aktion als lambda Funktionen
 * @param T Typ des Events
 */
class LambdaEventAction<T : Event>(private val action: (T) -> Unit) : EventAction<T>() {
    @EventDispatcherOnly
    override fun runAction(event: T) = action(event)
}
