package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MergedInventoryDisplay
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventHandler
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Vollständige GUI aus einem [ChestInventoryGui] und darunterliegenden [PlayerInventoryGui]
 * @param player Spieler, dem das Inventar gehört
 * @param title Titel des Kisten-Inventars
 * @param chestLines Zeilen des Kisten-Inventars (1 - 6)
 * @param eventDispatcher  Der Event-Manager für Events der Oberfläche
 * @param schedulerProvider Klasse zum registrieren von Minecraft schedulern
 * @param tickSpeed Die Schnelligkeit der GUI render Updates in Minecraft Ticks
 * @param static Ob die Komponente nur initial gerendert werden soll.
 * @param background Items für Slots, auf denen keine Komponente liegt
 * @see MergedInventoryDisplay
 * @see ComponentEndpoint
 * @see MinecraftGuiSurface
 */
@Suppress("unused")
abstract class MergedInventoryGui(
    player: Player,
    title: String,
    chestLines: Int,
    eventDispatcher: MinecraftGuiEventHandler,
    schedulerProvider: SchedulerProvider,
    tickSpeed: Long = 20,
    static: Boolean = false,
    background: ItemStack? = null
) : ComponentEndpoint(
    MinecraftGuiSurface(MergedInventoryDisplay(player, title, chestLines), eventDispatcher),
    schedulerProvider,
    tickSpeed,
    static,
    background
)
