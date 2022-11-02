package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.display.PlayerInventoryDisplay
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventHandler
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Vollständiges Minecraft-Spielerinventar GUI (Hot-bar und Menü mit Tastendruck auf "E")
 * @param player Spieler, dem das Inventar gehört
 * @param eventDispatcher  Der Event-Manager für Events der Oberfläche
 * @param schedulerProvider Klasse zum registrieren von Minecraft schedulern
 * @param renderTick Ob das Gui im Intervall von [tickSpeed] erneut gerendert werden soll
 * @param tickSpeed Die Schnelligkeit der GUI render Updates in Minecraft Ticks
 * @param static Ob die Komponente nur initial gerendert werden soll.
 * @param background Items für Slots, auf denen keine Komponente liegt
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen.
 * @see PlayerInventoryDisplay
 * @see ComponentEndpoint
 * @see MinecraftGuiSurface
 */
abstract class PlayerInventoryGui(
    player: Player,
    eventDispatcher: MinecraftGuiEventHandler,
    schedulerProvider: SchedulerProvider,
    tickSpeed: Long = 20,
    static: Boolean = false,
    renderTick: Boolean = true,
    smartRender: Boolean = true,
    background: ItemStack? = null,
    disableOtherInventories: Boolean = false
) : ComponentEndpoint(
    MinecraftGuiSurface(PlayerInventoryDisplay(player, disableOtherInventories), eventDispatcher),
    schedulerProvider,
    renderTick,
    tickSpeed,
    static,
    smartRender,
    background
)
