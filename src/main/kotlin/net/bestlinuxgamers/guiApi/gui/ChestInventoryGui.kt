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
 * @param player Spieler, dem das Inventar gehört
 * @param title Titel des Inventars
 * @param lines Zeilen des Inventars (1 - 6)
 * @param eventDispatcher  Der Event-Manager für Events der Oberfläche
 * @param schedulerProvider Klasse zum registrieren von Minecraft schedulern
 * @param tickSpeed Die Schnelligkeit der GUI render Updates in Minecraft Ticks
 * @param static Ob die Komponente nur initial gerendert werden soll.
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen.
 * @param background Items für Slots, auf denen keine Komponente liegt
 * @see ChestInventoryDisplay
 * @see ComponentEndpoint
 * @see MinecraftGuiSurface
 */
abstract class ChestInventoryGui(
    player: Player,
    title: String,
    lines: Int,
    eventDispatcher: MinecraftGuiEventHandler,
    schedulerProvider: SchedulerProvider,
    tickSpeed: Long = 20,
    static: Boolean = false,
    smartRender: Boolean = true,
    background: ItemStack? = null,
    disableOtherInventories: Boolean = false
) : ComponentEndpoint(
    MinecraftGuiSurface(ChestInventoryDisplay(player, title, lines, disableOtherInventories), eventDispatcher),
    schedulerProvider,
    tickSpeed,
    static,
    smartRender,
    background
)
