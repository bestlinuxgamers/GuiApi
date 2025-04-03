package net.bestlinuxgamers.guiApi.test.consoleGui

import io.mockk.mockk
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import net.bestlinuxgamers.guiApi.test.SchedulerRunnableExtractor
import org.bukkit.inventory.ItemStack

/**
 * @see ConsoleGuiSurface
 */
abstract class ConsoleGui(
    height: Int,
    width: Int,
    componentTick: Boolean = true,
    tickSpeed: Long = 1,
    directOnDemandRender: Boolean = false,
    autoRender: Boolean = true,
    autoRenderSpeed: Int = 1,
    static: Boolean = false,
    smartRender: Boolean = true,
    background: ItemStack? = null,
    debug: Boolean = false,
    schedulerProvider: SchedulerProvider = mockk(relaxed = true)
) : ComponentEndpoint(
    ConsoleGuiSurface(
        height,
        width,
        SchedulerRunnableExtractor(schedulerProvider).also { se -> se.addHook { it.run() } },
        debug = debug
    ),
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
