package net.bestlinuxgamers.guiApi.provider

import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventHandler
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class GuiInstancesProvider(val eventDispatcher: MinecraftGuiEventHandler, val schedulerProvider: SchedulerProvider) {
    constructor(plugin: JavaPlugin) : this(
        MinecraftGuiEventHandler(EventRegisterProvider(plugin)),
        SchedulerProvider(plugin)
    )
}
