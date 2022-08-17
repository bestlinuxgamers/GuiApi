package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

abstract class GuiSurface {

    private val registeredEndpoints: MutableSet<ComponentEndpoint> = mutableSetOf()

    fun getRegisteredEndpoints() = registeredEndpoints.toSet()

    internal fun registerEndpoint(endpoint: ComponentEndpoint) {
        registeredEndpoints.add(endpoint)
    }

    internal fun forEachEndpoint(action: (ComponentEndpoint) -> Unit) = registeredEndpoints.forEach(action)

    //open

    @SurfaceManagerOnly
    open fun open(items: Array<ItemStack?>) {
        if (isOpened()) return
        startListening()
        setupSurface(items)
    }

    @SurfaceManagerOnly
    abstract fun isOpened(): Boolean

    @SurfaceManagerOnly
    abstract fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?)

    @SurfaceManagerOnly
    abstract fun close()

    /**
     * Erstellt, speichert und öffnet ein Surface
     * @param items Items, welche das Surface beinhalten soll
     */
    internal abstract fun setupSurface(items: Array<ItemStack?>)

    internal abstract fun generateReserved(): ReservedSlots

    internal abstract fun startListening()

    internal abstract fun stopListening()

    @EventDispatcherOnly
    internal abstract fun performClose()

    //dispatchers

    @EventDispatcherOnly
    abstract fun dispatchClickEvent(event: InventoryClickEvent)

    @EventDispatcherOnly
    abstract fun dispatchCloseEvent(event: InventoryCloseEvent)


}
