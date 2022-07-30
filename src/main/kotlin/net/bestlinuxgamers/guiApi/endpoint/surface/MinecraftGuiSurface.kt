package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import org.bukkit.entity.Player

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

    companion object {
        const val GUI_WIDTH = 9
    }
}
