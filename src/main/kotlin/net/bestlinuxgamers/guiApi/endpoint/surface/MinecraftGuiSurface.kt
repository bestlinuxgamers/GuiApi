package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class MinecraftGuiSurface(
    internal val player: Player,
    internal val lines: Int,
) : GuiSurface() {

    @SurfaceManagerOnly
    override fun generateReserved(): ReservedSlots = ReservedSlots(lines, GUI_WIDTH)

    companion object {
        const val GUI_WIDTH = 9
    }
}

abstract class ListeningMinecraftGuiSurface(
    player: Player,
    lines: Int,
    private val eventDispatcher: MinecraftGuiEventDispatcher
) : MinecraftGuiSurface(player, lines) {

    internal abstract var inventory: Inventory?

    override fun startListening() {
        inventory?.let { eventDispatcher.registerListening(it, this) }
    }

    override fun stopListening() {
        eventDispatcher.unregisterListening(this)
    }
}
