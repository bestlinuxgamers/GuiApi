package net.bestlinuxgamers.guiApi.endpoint

import io.mockk.every
import io.mockk.mockk
import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.essentials.ItemComponent
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurfaceInterface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import net.bestlinuxgamers.guiApi.templates.server.MinecraftItemExtension
import net.bestlinuxgamers.guiApi.templates.server.MinecraftServerMockObj
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.*

internal class ComponentEndpointTest {

    @Test
    fun testBeforeRender() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)

        @OptIn(SurfaceManagerOnly::class)
        every { mockSurface.generateReserved() } returns ReservedSlots(1, 1)

        lateinit var renderResults: Array<ItemStack?>
        @OptIn(SurfaceManagerOnly::class)
        every { mockSurface.updateItems(any(), any()) } answers {
            renderResults = it.invocation.args[0] as Array<ItemStack?>
        }
        @OptIn(SurfaceManagerOnly::class)
        every { mockSurface.open(any()) } answers {
            renderResults = it.invocation.args[0] as Array<ItemStack?>
        }

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)

        lateinit var schedulerRunnable: Runnable
        every { mockScheduler.runTaskTimerAsynchronously(any(), any(), any()) } answers {
            schedulerRunnable = it.invocation.args[2] as Runnable
            mockk(relaxed = true)
        }

        val ep = object : ComponentEndpoint(mockSurface, mockScheduler) {
            override fun setUp() {}

            override fun beforeRender(frame: Long) {}
        }

        class TestComponent : GuiComponent(ReservedSlots(1, 1), false) {
            override fun setUp() {}

            override fun beforeRender(frame: Long) {
                when (frame) {
                    1.toLong() -> {
                        removeAllComponents()
                        setComponent(ItemComponent(ItemStack(Material.STONE)), 0)
                    }
                }
            }
        }

        val instance = TestComponent()
        val target1 = arrayOf(ItemStack(Material.STICK))
        val target2 = arrayOf(ItemStack(Material.STONE))

        instance.setComponent(ItemComponent(ItemStack(Material.STICK)), 0)
        ep.setComponent(instance, 0)

        ep.open()
        Assertions.assertArrayEquals(target1, renderResults)

        schedulerRunnable.run()
        Assertions.assertArrayEquals(target2, renderResults)
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun initServer() {
            MinecraftServerMockObj.apply { addExtension(MinecraftItemExtension()) }.apply()
        }
    }

}