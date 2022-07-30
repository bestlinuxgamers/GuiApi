package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.ItemGuiSurface
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class ItemGui(
    player: Player,
    title: String,
    lines: Int,
    eventDispatcher: MinecraftGuiEventDispatcher,
    schedulerProvider: SchedulerProvider,
    tickSpeed: Long = 20,
    static: Boolean = false,
    background: ItemStack? = null
) : ComponentEndpoint(
    ItemGuiSurface(player, title, lines, eventDispatcher),
    schedulerProvider,
    tickSpeed,
    static,
    background
)


