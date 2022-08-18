package net.bestlinuxgamers.guiApi.provider

import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import org.bukkit.plugin.java.JavaPlugin

class GuiInstancesProvider(val eventDispatcher: MinecraftGuiEventDispatcher, val schedulerProvider: SchedulerProvider) {
    constructor(plugin: JavaPlugin) : this(
        MinecraftGuiEventDispatcher(EventRegisterProvider(plugin)),
        SchedulerProvider(plugin)
    )
}
