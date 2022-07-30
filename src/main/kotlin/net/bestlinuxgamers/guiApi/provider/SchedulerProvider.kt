package net.bestlinuxgamers.guiApi.provider

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SchedulerProvider(private val plugin: JavaPlugin) {

    /**
     * Startet einen Scheduler und übergibt [plugin] automatisch
     * @see [org.bukkit.scheduler.BukkitScheduler.runTaskTimerAsynchronously]
     */
    internal fun runTaskTimerAsynchronously(delay: Long, period: Long, task: Runnable) =
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period)

}
