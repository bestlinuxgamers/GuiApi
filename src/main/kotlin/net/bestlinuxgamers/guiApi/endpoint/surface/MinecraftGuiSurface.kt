package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay
import net.bestlinuxgamers.guiApi.endpoint.surface.util.NoEndpointRegisteredException
import net.bestlinuxgamers.guiApi.endpoint.surface.util.SurfaceAlreadyInUseException
import net.bestlinuxgamers.guiApi.event.*
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

/**
 * Klasse verbindet ein [ComponentEndpoint] mit einer grafischen Minecraft Inventar Oberfläche ([MinecraftDisplay])
 * und dem [MinecraftGuiEventHandler].
 * [MinecraftGuiEventHandler] und [MinecraftDisplay] wurde getrennt,
 * damit ein [MinecraftDisplay] von einem anderen Display ohne Probleme verwendet werden kann.
 * @param display Die Oberfläche des Inventars
 * @param eventHandler Der Event-Manager für Events des [display]
 */
open class MinecraftGuiSurface(
    private val display: MinecraftDisplay,
    private val eventHandler: MinecraftGuiEventHandler
) : GuiSurfaceInterface {

    private var registeredEndpoint: ComponentEndpoint? = null
    private val activeRegistrations: MutableSet<EventRegistration<out EventListenerAdapter<out Event>, out Event>> =
        mutableSetOf()

    /**
     * @throws SurfaceAlreadyInUseException wenn sich bereits registriert wurde
     */
    @SurfaceManagerOnly
    override fun registerEndpoint(endpoint: ComponentEndpoint) {
        if (registeredEndpoint != null) throw SurfaceAlreadyInUseException()
        registeredEndpoint = endpoint
        registerEvents(endpoint)
    }

    /**
     * Registriert alle dem Surface bekannten [EventRegistration]s.
     * Außerdem erstellt es für die klick und close Aktion eine [EventRegistration].
     * @see registerRegistration
     */
    private fun registerEvents(endpoint: ComponentEndpoint) {
        registerRegistration(
            EventRegistration(
                display.clickEventIdentifier,
                @OptIn(EventDispatcherOnly::class)
                LambdaEventAction { endpoint.performClick(display.getComponentSlot(it), it) })
        )
        registerRegistration(
            EventRegistration(
                display.closeActionEventIdentifier,
                @OptIn(EventDispatcherOnly::class) LambdaEventAction { endpoint.performClose() })
        )
        display.eventRegistrations.forEach { registerRegistration(it) }
    }

    /**
     * Registriert eine [EventRegistration] und fügt diese einer Liste hinzu,
     * damit sie später wieder unregistriert werden können.
     * @see MinecraftGuiEventHandler.registerDispatcher
     */
    private fun registerRegistration(registration: EventRegistration<out EventListenerAdapter<out Event>, out Event>) {
        eventHandler.registerDispatcher(registration)
        activeRegistrations.add(registration)
    }

    @SurfaceManagerOnly
    override fun unregisterEndpoint() {
        registeredEndpoint = null
        activeRegistrations.removeAll { eventHandler.unregisterDispatcher(it); true }
    }

    /**
     * @throws NoEndpointRegisteredException Falls kein Endpoint registriert ist und
     * sich deshalb nicht für Events registriert werden kann
     */
    @SurfaceManagerOnly
    override fun open(items: Array<ItemStack?>) {
        display.open(items)
    }


    @SurfaceManagerOnly
    override fun isOpened(): Boolean = display.isOpened()


    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) =
        display.updateItems(items, lastItems)

    @SurfaceManagerOnly
    override fun close() = display.close()

    @EventDispatcherOnly
    override fun onClose() {
        display.onClose()
    }

    @SurfaceManagerOnly
    override fun generateReserved(): ReservedSlots = display.reservedSlots
}
