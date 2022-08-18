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

    val surfaceSize by lazy { lines * GUI_WIDTH }

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
        if (event.slot < 0) return

        //TODO wenn ins Spielerinv gedrückt wird gibt es keine schöne Möglichkeit ohne Fehler den dispatcher zu beenden
        val clickedSlot = getComponentIndex(event)
        event.isCancelled = true //TODO manuell entscheiden lassen
        forEachEndpoint { it.performClick(clickedSlot, event) }
    }

    @EventDispatcherOnly
    override fun dispatchCloseEvent(event: InventoryCloseEvent) {
        forEachEndpoint { it.performClose() } //TODO auch PlayerInventorySurface wird geclosed
    }

    companion object {
        const val GUI_WIDTH = 9
    }
}
