package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.PlayerInventorySurface
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class PlayerInventoryGui(
    player: Player,
    lines: Int,
    eventDispatcher: MinecraftGuiEventDispatcher,
    schedulerProvider: SchedulerProvider,
    tickSpeed: Long = 20,
    static: Boolean = false,
    background: ItemStack? = null
) : ComponentEndpoint(
    PlayerInventorySurface(player, lines, eventDispatcher),
    schedulerProvider,
    tickSpeed,
    static,
    background
)


