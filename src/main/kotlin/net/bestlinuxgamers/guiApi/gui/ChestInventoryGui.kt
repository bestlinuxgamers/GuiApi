package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.display.ChestInventoryDisplay
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventHandler
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Vollständiges Minecraft-Kisten-Inventar
 * @param player [ChestInventoryDisplay.player]
 * @param title Titel des Inventars
 * @param lines Zeilen des Inventars (1 - 6)
 * @param eventHandler [MinecraftGuiSurface.eventHandler]
 * @param schedulerProvider [ComponentEndpoint.schedulerProvider]
 * @param componentTick [ComponentEndpoint.componentTick]
 * @param tickSpeed [ComponentEndpoint.tickSpeed]
 * @param directOnDemandRender [ComponentEndpoint.directOnDemandRender]
 * @param autoRender [ComponentEndpoint.autoRender]
 * @param autoRenderSpeed [ComponentEndpoint.autoRenderSpeed]
 * @param static [ComponentEndpoint.static]
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen ([ComponentEndpoint.smartRender])
 * @param background [ComponentEndpoint.renderFallback]
 * @param disableOtherInventories Ob andere Inventare deaktiviert werden sollen, während die GUI geöffnet ist.
 * @see ChestInventoryDisplay
 * @see ComponentEndpoint
 * @see MinecraftGuiSurface
 */
abstract class ChestInventoryGui(
    player: Player,
    title: String,
    lines: Int,
    eventHandler: MinecraftGuiEventHandler,
    schedulerProvider: SchedulerProvider?,
    componentTick: Boolean = true,
    tickSpeed: Long = 20,
    directOnDemandRender: Boolean = false,
    autoRender: Boolean = true,
    autoRenderSpeed: Int = 1,
    static: Boolean = false,
    smartRender: Boolean = true,
    background: ItemStack? = null,
    disableOtherInventories: Boolean = false
) : ComponentEndpoint(
    MinecraftGuiSurface(ChestInventoryDisplay(player, title, lines, disableOtherInventories), eventHandler),
    schedulerProvider,
    componentTick = componentTick,
    tickSpeed = tickSpeed,
    directOnDemandRender = directOnDemandRender,
    autoRender = autoRender,
    autoRenderSpeed = autoRenderSpeed,
    static = static,
    smartRender = smartRender,
    background = background
)
