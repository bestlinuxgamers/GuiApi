package net.bestlinuxgamers.guiApi.provider

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Klasse zum Registrieren von Events für das Bukkit Event-System, ohne die plugin Instanz zu übergeben.
 */
@Suppress("unused")
class EventRegisterProvider(private val plugin: JavaPlugin) {

    fun registerListener(listener: Listener) = Bukkit.getPluginManager().registerEvents(listener, plugin)

    fun registerListeners(listeners: Collection<Listener>) {
        with(Bukkit.getPluginManager()) {
            listeners.forEach { registerEvents(it, plugin) }
        }
    }

}
