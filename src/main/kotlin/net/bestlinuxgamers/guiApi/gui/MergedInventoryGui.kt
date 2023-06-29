package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MergedInventoryDisplay
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventHandler
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Vollständige GUI aus einem [ChestInventoryGui] und darunterliegenden [PlayerInventoryGui]
 * @param player [MergedInventoryDisplay.player]
 * @param title Titel des Kisten-Inventars
 * @param chestLines Zeilen des Kisten-Inventars (1 - 6)
 * @param eventHandler  Der Event-Manager für Events der Oberfläche
 * @param schedulerProvider [ComponentEndpoint.schedulerProvider]
 * @param componentTick [GuiComponent.componentTick]
 * @param tickSpeed [GuiComponent.tickSpeed]
 * @param directOnDemandRender [ComponentEndpoint.directOnDemandRender]
 * @param autoRender [ComponentEndpoint.autoRender]
 * @param autoRenderSpeed [ComponentEndpoint.autoRenderSpeed]
 * @param static [GuiComponent.static]
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen ([GuiComponent.smartRender])
 * @param background [ComponentEndpoint.renderFallback]
 * @see MergedInventoryDisplay
 * @see ComponentEndpoint
 * @see MinecraftGuiSurface
 */
@Suppress("unused")
abstract class MergedInventoryGui(
    player: Player,
    title: String,
    chestLines: Int,
    eventHandler: MinecraftGuiEventHandler,
    schedulerProvider: SchedulerProvider?,
    componentTick: Boolean = true,
    tickSpeed: Long = 20,
    directOnDemandRender: Boolean = false,
    autoRender: Boolean = true,
    autoRenderSpeed: Int = 1,
    static: Boolean = false,
    smartRender: Boolean = true,
    background: ItemStack? = null
) : ComponentEndpoint(
    MinecraftGuiSurface(MergedInventoryDisplay(player, title, chestLines), eventHandler),
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
