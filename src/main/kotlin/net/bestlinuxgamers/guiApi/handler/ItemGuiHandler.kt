package net.bestlinuxgamers.guiApi.handler

import net.bestlinuxgamers.guiApi.ItemGui
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

/**
 * Klasse zum Verarbeiten und verteilen von GUI-spezifischen Events.
 * Außerdem erhalten [ItemGui]s so die Möglichkeit BukkitScheduler zu benutzen,
 * da dort das [plugin] angegeben werden muss.
 * @param plugin Haupt Klasse des Minecraft Plugins, welches [ItemGui] verwendet
 * @see ItemGui
 * @see ItemGuiListener
 * @see Bukkit.getScheduler
 */
class ItemGuiHandler(private val plugin: JavaPlugin) {

    private val listeningComponents: MutableMap<Player, ItemGui> = mutableMapOf()

    init {
        registerEvents()
    }

    /**
     * Registriert den [ItemGuiListener] bei Bukkit
     * @see PluginManager.registerEvents
     */
    private fun registerEvents() {
        val pluginManager: PluginManager = Bukkit.getPluginManager()
        pluginManager.registerEvents(ItemGuiListener(this), plugin)
    }

    //register gui

    /**
     * Registriert ein [ItemGui] für den Empfang von Events
     * @param player Spieler, welchem das [gui] gehört
     * @param gui Gui, welches Events empfangen soll
     */
    internal fun registerListeningGui(player: Player, gui: ItemGui) {
        listeningComponents[player]?.performClose()
        listeningComponents[player] = gui
    }

    /**
     * Unregistriert ein [ItemGui] für den Empfang von Events
     * @param gui Gui, welches keine Events mehr empfangen soll
     */
    @Suppress("unused")
    internal fun unregisterListeningGui(gui: ItemGui) {
        listeningComponents.forEach { (key, value) ->
            if (value === gui) unregisterListeningGui(key)
            return@forEach
        }
    }

    /**
     * Unregistriert ein [ItemGui] für den Empfang von Events
     * @param player Spieler, wessen Gui keine Events mehr empfangen soll
     */
    internal fun unregisterListeningGui(player: Player) {
        listeningComponents.remove(player)
    }

    //dispatchers

    /**
     * Verteilt ein [InventoryClickEvent] an das registrierte Gui des Spielers,
     * welcher das [event] ausgelöst hat.
     * Sollte nur vom [ItemGuiListener] ausgeführt werden.
     * @param event ausgelöstes Event
     * @see registerListeningGui
     */
    internal fun dispatchClickEvent(event: InventoryClickEvent) {
        listeningComponents[event.whoClicked]?.performClick(event)
    }

    /**
     * Verteilt ein [InventoryCloseEvent] an das registrierte Gui des Spielers,
     * welcher das [event] ausgelöst hat.
     * Sollte nur vom [ItemGuiListener] ausgeführt werden.
     * @param event ausgelöstes Event
     * @see registerListeningGui
     */
    internal fun dispatchCloseEvent(event: InventoryCloseEvent) {
        listeningComponents[event.player]?.performClose()
    }

    //scheduler

    /**
     * Startet einen Scheduler und übergibt [plugin] automatisch
     * @see [org.bukkit.scheduler.BukkitScheduler.runTaskTimerAsynchronously]
     */
    internal fun runTaskTimerAsynchronously(delay: Long, period: Long, task: Runnable) =
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period)
}
