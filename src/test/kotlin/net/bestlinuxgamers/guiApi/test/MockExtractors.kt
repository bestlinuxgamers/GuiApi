package net.bestlinuxgamers.guiApi.test

import io.mockk.every
import io.mockk.mockk
import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurfaceInterface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import net.bestlinuxgamers.guiApi.test.templates.MockExtractor
import org.bukkit.inventory.ItemStack

class SchedulerRunnableExtractor(input: SchedulerProvider, onlyOneSet: Boolean = true) :
    MockExtractor<SchedulerProvider, Runnable>(input, onlyOneSet) {
    override fun setup(input: SchedulerProvider) {
        every { input.runTaskTimerAsynchronously(any(), any(), any()) } answers {
            setValue(it.invocation.args[2] as Runnable)
            mockk(relaxed = true)
        }
    }
}

class RenderResultExtractor(input: GuiSurfaceInterface) :
    MockExtractor<GuiSurfaceInterface, Array<ItemStack?>>(input, false) {
    override fun setup(input: GuiSurfaceInterface) {
        @OptIn(SurfaceManagerOnly::class)
        every { input.updateItems(any(), any()) } answers {
            @Suppress("UNCHECKED_CAST")
            setValue(it.invocation.args[0] as Array<ItemStack?>)
        }
        @OptIn(SurfaceManagerOnly::class)
        every { input.open(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            setValue(it.invocation.args[0] as Array<ItemStack?>)
        }
    }
}
