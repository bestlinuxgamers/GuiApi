package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay
import net.bestlinuxgamers.guiApi.endpoint.surface.util.NoEndpointRegisteredException
import net.bestlinuxgamers.guiApi.endpoint.surface.util.SurfaceAlreadyInUseException
import net.bestlinuxgamers.guiApi.endpoint.surface.util.SurfaceEventReceiver
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import org.bukkit.inventory.ItemStack

/**
 * Klasse verbindet eine grafische Minecraft Inventar Oberfläche ([MinecraftDisplay]) und den [MinecraftGuiEventDispatcher]
 * @param display Die Oberfläche des Inventars
 * @param eventDispatcher Der Event-Manager für Events des [display]
 */
open class MinecraftGuiSurface(
    private val display: MinecraftDisplay,
    private val eventDispatcher: MinecraftGuiEventDispatcher
) : GuiSurfaceInterface {

    private var registeredReceiver: SurfaceEventReceiver? = null

    /**
     * @throws SurfaceAlreadyInUseException wenn sich bereits registriert wurde
     */
    @SurfaceManagerOnly
    override fun registerEndpoint(endpoint: ComponentEndpoint) {
        if (registeredReceiver != null) throw SurfaceAlreadyInUseException()
        registeredReceiver = SurfaceEventReceiver(display, endpoint)
    }

    @SurfaceManagerOnly
    override fun unregisterEndpoint() {
        registeredReceiver = null
    }

    /**
     * @throws NoEndpointRegisteredException Falls kein Endpoint registriert ist und
     * sich deshalb nicht für Events registriert werden kann
     */
    @SurfaceManagerOnly
    override fun open(items: Array<ItemStack?>) {
        val receiver = registeredReceiver ?: throw NoEndpointRegisteredException()
        eventDispatcher.registerListening(
            display.player,
            display.eventIdentifier,
            receiver
        )
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
        @OptIn(SurfaceManagerOnly::class)
        registeredReceiver?.let { eventDispatcher.unregisterListening(display.player, it) }
        display.onClose()
    }

    @SurfaceManagerOnly
    override fun generateReserved(): ReservedSlots = display.reservedSlots
}
