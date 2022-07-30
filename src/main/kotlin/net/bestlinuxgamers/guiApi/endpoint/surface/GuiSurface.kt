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

    private fun forEachEndpoint(action: (ComponentEndpoint) -> Unit) = registeredEndpoints.forEach(action)

    //open

    @SurfaceManagerOnly
    fun open(items: Array<ItemStack?>) {
        if (isOpened()) return
        startListening()
        setupSurface(items)
    }

    internal abstract fun isOpened(): Boolean

    /**
     * Erstellt, speichert und öffnet ein Surface
     * @param items Items, welche das Surface beinhalten soll
     */
    internal abstract fun setupSurface(items: Array<ItemStack?>)


    @SurfaceManagerOnly
    abstract fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?)

    @SurfaceManagerOnly
    abstract fun close()

    internal abstract fun generateReserved(): ReservedSlots

    internal abstract fun getComponentIndex(event: InventoryClickEvent): Int

    internal abstract fun startListening()

    internal abstract fun stopListening()

    @EventDispatcherOnly
    internal abstract fun performClose()

    //dispatchers

    @EventDispatcherOnly
    internal fun dispatchClickEvent(event: InventoryClickEvent) {
        val clickedSlot = getComponentIndex(event)
        forEachEndpoint { it.performClick(clickedSlot, event) }
    }

    @EventDispatcherOnly
    internal fun dispatchCloseEvent(event: InventoryCloseEvent) {
        forEachEndpoint { it.performClose() }
    }


}
