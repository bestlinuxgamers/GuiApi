package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

abstract class MinecraftGuiSurface(
    internal val player: Player,
    internal val lines: Int,
    private val eventDispatcher: MinecraftGuiEventDispatcher
) : GuiSurface() {

    override fun generateReserved(): ReservedSlots = ReservedSlots(lines, GUI_WIDTH)

    override fun startListening() {
        eventDispatcher.registerListening(player, this)
    }

    override fun stopListening() {
        eventDispatcher.unregisterListening(this)
    }

    internal abstract fun getComponentIndex(event: InventoryClickEvent): Int

    //Dispatcher

    @EventDispatcherOnly
    override fun dispatchClickEvent(event: InventoryClickEvent) {
        val clickedSlot = getComponentIndex(event)
        forEachEndpoint { it.performClick(clickedSlot, event) }
    }

    @EventDispatcherOnly
    override fun dispatchCloseEvent(event: InventoryCloseEvent) {
        forEachEndpoint { it.performClose() }
    }

    companion object {
        const val GUI_WIDTH = 9
    }
}
