package net.bestlinuxgamers.guiApi.event

import net.bestlinuxgamers.guiApi.provider.EventRegisterProvider
import org.bukkit.event.Event
import kotlin.reflect.KClass

/**
 * Klasse zum Registrieren von [EventRegistration]s beim zugehörigen [EventListenerAdapter]
 * @param eventRegisterProvider Provider zum Registrieren von Listenern bei Bukkit
 */
class MinecraftGuiEventHandler(private val eventRegisterProvider: EventRegisterProvider) {

    /**
     * Instanzen aller [EventListenerAdapter], welche dieser Handler unterstützt.
     * Neue Adapter müssen hier hinzugefügt werden.
     */
    private val adapters: Set<EventListenerAdapter<out Event>> = setOf(
        ClickEventListenerAdapter(),
        CloseEventListenerAdapter(),
        QuitEventListenerAdapter()
    )

    private val adapterClasses: Map<KClass<out EventListenerAdapter<out Event>>, EventListenerAdapter<out Event>> =
        adapters.associateBy { it::class }

    init {
        registerListener()
    }

    /**
     * Registriert alle [adapters] als Listener beim Bukkit Event-System.
     * @see EventRegisterProvider.registerListeners
     */
    private fun registerListener() {
        eventRegisterProvider.registerListeners(adapters)
    }

    /**
     * Registriert eine [EventRegistration] beim zugehörigen [EventListenerAdapter].
     * @param registration Die Aktionen des Events
     */
    fun registerDispatcher(registration: EventRegistration<out EventListenerAdapter<out Event>, out Event>) {
        adapterClasses[registration.adapterClass]?.addRegistration(registration)
    }

    /**
     * Entfernt eine bestehende Registrierung einer [EventRegistration] von dem zugehörigen [EventListenerAdapter].
     * @param registration Registrierung, die entfernt werden soll
     */
    fun unregisterDispatcher(registration: EventRegistration<out EventListenerAdapter<out Event>, out Event>) {
        adapterClasses[registration.adapterClass]?.removeRegistration(registration)
    }


}
