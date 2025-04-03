package net.bestlinuxgamers.guiApi.endpoint

import io.mockk.every
import io.mockk.mockk
import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.essentials.ItemComponent
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurfaceInterface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import net.bestlinuxgamers.guiApi.test.RenderResultExtractor
import net.bestlinuxgamers.guiApi.test.SchedulerRunnableExtractor
import net.bestlinuxgamers.guiApi.test.templates.server.MinecraftItemExtension
import net.bestlinuxgamers.guiApi.test.templates.server.MinecraftServerMockObj
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class ComponentEndpointTest {

    @Test
    fun testOpen() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)

        @OptIn(SurfaceManagerOnly::class)
        every { mockSurface.generateReserved() } returns ReservedSlots(1, 1)
        var opened = false
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.open(any()) } answers { opened = true }
        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerExtractor.addHook { it.run() }

        var lastTick: Long? = null
        var lastFrame: Long? = null
        var renderedFrame: Long? = null

        val ep = object : ComponentEndpoint(mockSurface, mockScheduler, componentTick = true) {
            override fun beforeRender(frame: Long) {
                renderedFrame = frame
            }

            override fun onComponentTick(tick: Long, frame: Long) {
                if (lastTick == null && tick != FIRST_COMPONENT_TICK) throw IllegalStateException("Tick $FIRST_COMPONENT_TICK not called")
                if (tick == FIRST_COMPONENT_TICK && opened) throw IllegalStateException("GUI already opened at first Tick")
                lastTick = tick
                lastFrame = frame
            }
        }

        ep.open()
        Assertions.assertTrue(opened)
        Assertions.assertEquals(FIRST_COMPONENT_TICK, lastTick)
        // Always next frame to render in Tick
        Assertions.assertEquals(1, lastFrame)
        // Only one render by the open() function
        Assertions.assertEquals(1, renderedFrame)
    }

    @Test
    fun testBeforeRender() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)

        @OptIn(SurfaceManagerOnly::class)
        every { mockSurface.generateReserved() } returns ReservedSlots(1, 1)
        val renderResults = RenderResultExtractor(mockSurface)

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerRunnableExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerRunnableExtractor.addHook { it.run() }

        var lastFrame: Long? = null
        val ep = object : ComponentEndpoint(
            mockSurface, mockScheduler,
            smartRender = true,
            componentTick = true,
            tickSpeed = 1
        ) {
            override fun onComponentTick(tick: Long, frame: Long) {}

            override fun beforeRender(frame: Long) {
                lastFrame = frame
            }
        }

        var lastScFrame: Long? = null

        class TestComponent : GuiComponent(ReservedSlots(1, 1), static = false) {
            override fun onComponentTick(tick: Long, frame: Long) {}

            override fun beforeRender(frame: Long) {
                lastScFrame = frame
                if (frame == 2.toLong()) {
                    removeAllComponents()
                    setComponent(ItemComponent(ItemStack(Material.STONE)), 0)
                }
            }
        }

        val instance = TestComponent()
        val target1 = arrayOf(ItemStack(Material.STICK))
        val target2 = arrayOf(ItemStack(Material.STONE))

        instance.setComponent(ItemComponent(ItemStack(Material.STICK)), 0)
        ep.setComponent(instance, 0)

        ep.open()
        Assertions.assertEquals(1, lastFrame)
        Assertions.assertEquals(1, lastScFrame)
        Assertions.assertArrayEquals(target1, renderResults.getDifferent())

        // to trigger changes that will be rendered
        instance.removeAllComponents()
        ep.triggerReRender(1)
        // run component tick that will start a render
        schedulerRunnableExtractor.get()!!.run()

        Assertions.assertEquals(2, lastFrame)
        Assertions.assertEquals(2, lastScFrame)
        Assertions.assertArrayEquals(target2, renderResults.getDifferent())
    }

    @Test
    fun testOnComponentTick() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.generateReserved() } returns ReservedSlots(1, 1)

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerExtractor.addHook { it.run() }

        var lastCallTick: Long? = null
        var lastCallFrame: Long? = null

        val ep = object : ComponentEndpoint(
            mockSurface,
            mockScheduler,
            componentTick = true,
            tickSpeed = 1,
            autoRender = false,
            background = ItemStack(Material.STICK),
        ) {
            override fun beforeRender(frame: Long) {}

            override fun onComponentTick(tick: Long, frame: Long) {
                lastCallTick = tick
                lastCallFrame = frame
            }
        }

        var lastScCallTick: Long? = null
        var lastScCallFrame: Long? = null

        ep.setComponent(object : GuiComponent(
            ReservedSlots(1, 1),
            componentTick = true,
            tickSpeed = 2,
        ) {
            override fun beforeRender(frame: Long) {}

            override fun onComponentTick(tick: Long, frame: Long) {
                lastScCallTick = tick
                lastScCallFrame = frame
            }

        }, 0)

        ep.open()

        val expectCall: Long = 0
        val expectFrame: Long = 1
        Assertions.assertEquals(expectCall, lastCallTick)
        Assertions.assertEquals(expectFrame, lastCallFrame)

        Assertions.assertEquals(expectCall, lastScCallTick)
        Assertions.assertEquals(expectFrame, lastScCallFrame)

        val scheduler = schedulerExtractor.get()!!
        scheduler.run()
        val expectFrame2: Long = 2

        Assertions.assertEquals(1, lastCallTick)
        Assertions.assertEquals(expectFrame2, lastCallFrame)

        // no change
        Assertions.assertEquals(expectCall, lastScCallTick)
        Assertions.assertEquals(expectFrame, lastScCallFrame)

        ep.triggerReRender(1)
        scheduler.run()

        Assertions.assertEquals(2, lastCallTick)
        Assertions.assertEquals(expectFrame2, lastCallFrame)

        Assertions.assertEquals(1, lastScCallTick)
        Assertions.assertEquals(expectFrame2, lastScCallFrame)

        scheduler.run()

        Assertions.assertEquals(3, lastCallTick)
        Assertions.assertEquals(expectFrame2, lastCallFrame)

        // no change
        Assertions.assertEquals(1, lastScCallTick)
        Assertions.assertEquals(expectFrame2, lastScCallFrame)

    }

    @Test
    fun testTriggerReRender() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.generateReserved() } returns ReservedSlots(1, 1)
        var itemsUpdated = 0
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.updateItems(any(), any()) } answers { itemsUpdated++ }

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerExtractor.addHook { it.run() }

        val ep = object : ComponentEndpoint(
            mockSurface,
            mockScheduler,
            componentTick = true,
            tickSpeed = 1,
            autoRender = false,
            directOnDemandRender = true
        ) {
            override fun beforeRender(frame: Long) {}
            override fun onComponentTick(tick: Long, frame: Long) {}
        }
        ep.open()
        ep.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 0)

        ep.triggerReRender(0)

        Assertions.assertEquals(1, itemsUpdated)

        ep.removeAllComponents()

        val scheduler = schedulerExtractor.get()!!
        scheduler.run()
        Assertions.assertEquals(1, itemsUpdated)

        ep.triggerReRender(10)
        ep.triggerReRender(2)
        ep.triggerReRender(5)

        scheduler.run()
        Assertions.assertEquals(1, itemsUpdated)
        scheduler.run()
        Assertions.assertEquals(2, itemsUpdated)

    }

    @Test
    fun testTriggerReRenderDirectForbidden() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.generateReserved() } returns ReservedSlots(1, 1)
        var itemsUpdated = 0
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.updateItems(any(), any()) } answers { itemsUpdated++ }

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerExtractor.addHook { it.run() }

        val ep = object : ComponentEndpoint(
            mockSurface,
            mockScheduler,
            componentTick = true,
            tickSpeed = 1,
            autoRender = false,
            directOnDemandRender = false
        ) {
            override fun beforeRender(frame: Long) {}
            override fun onComponentTick(tick: Long, frame: Long) {}
        }
        ep.open()
        ep.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 0)

        ep.triggerReRender(0)

        Assertions.assertEquals(0, itemsUpdated)

        schedulerExtractor.get()!!.run()
        Assertions.assertEquals(1, itemsUpdated)
    }

    @Test
    fun testAutoRenderBasic() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.generateReserved() } returns ReservedSlots(1, 1)
        var itemsUpdated = 0
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.updateItems(any(), any()) } answers { itemsUpdated++ }

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerExtractor.addHook { it.run() }

        val ep = object : ComponentEndpoint(
            mockSurface,
            mockScheduler,
            componentTick = true,
            tickSpeed = 1,
            autoRender = true,
            autoRenderSpeed = 2,
            smartRender = true,
        ) {
            override fun beforeRender(frame: Long) {}
            override fun onComponentTick(tick: Long, frame: Long) {}
        }
        ep.open()
        ep.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 0)

        Assertions.assertEquals(0, itemsUpdated)

        val scheduler = schedulerExtractor.get()!!
        scheduler.run()

        Assertions.assertEquals(0, itemsUpdated)

        scheduler.run()

        Assertions.assertEquals(1, itemsUpdated)
    }

    @Test
    fun testAutoRenderNested() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.generateReserved() } returns ReservedSlots(1, 3)
        var itemsUpdated = 0
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.updateItems(any(), any()) } answers { itemsUpdated++ }

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler)
        schedulerExtractor.addHook { it.run() }

        val ep = object : ComponentEndpoint(
            mockSurface,
            mockScheduler,
            componentTick = true,
            tickSpeed = 1,
            autoRender = true,
            autoRenderSpeed = 1,
            smartRender = true,
        ) {
            override fun beforeRender(frame: Long) {}
            override fun onComponentTick(tick: Long, frame: Long) {}
        }

        val comp = object : GuiComponent(ReservedSlots(1, 1), autoRender = false) {
            override fun beforeRender(frame: Long) {}
            override fun onComponentTick(tick: Long, frame: Long) {}
        }

        val autoComp = object : GuiComponent(ReservedSlots(1, 1), autoRender = true, autoRenderSpeed = 2) {
            override fun beforeRender(frame: Long) {}
            override fun onComponentTick(tick: Long, frame: Long) {}
        }

        ep.setComponent(comp, 0)
        ep.setComponent(autoComp, 1)
        ep.open()

        Assertions.assertEquals(0, itemsUpdated)
        val scheduler = schedulerExtractor.get()!!

        ep.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 2)

        scheduler.run()
        Assertions.assertEquals(1, itemsUpdated)

        scheduler.run()
        Assertions.assertEquals(1, itemsUpdated)

        autoComp.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 0)
        comp.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 0)

        scheduler.run()
        Assertions.assertEquals(1, itemsUpdated)

        scheduler.run()
        Assertions.assertEquals(2, itemsUpdated)
    }

    @Test
    fun testOpenAfterClose() {
        val mockSurface: GuiSurfaceInterface = mockk(relaxed = true)
        every { @OptIn(SurfaceManagerOnly::class) mockSurface.generateReserved() } returns ReservedSlots(1, 1)
        var opened = false

        val mockScheduler: SchedulerProvider = mockk(relaxed = true)
        val schedulerExtractor = SchedulerRunnableExtractor(mockScheduler, onlyOneSet = false)
        schedulerExtractor.addHook { it.run() }

        var lastTick: Long? = null
        var lastRender: Long? = null
        val ep = object : ComponentEndpoint(
            mockSurface,
            mockScheduler,
            componentTick = true,
            tickSpeed = 1,
            autoRender = false,
            background = ItemStack(Material.STICK),
        ) {
            override fun beforeRender(frame: Long) {
                lastRender = frame
            }

            override fun onComponentTick(tick: Long, frame: Long) {
                lastTick = tick
            }
        }
        ep.setComponent(ItemComponent(ItemStack(Material.BARRIER)), 0)

        every { @OptIn(SurfaceManagerOnly::class) mockSurface.open(any()) } answers {
            if (opened) {
                Assertions.assertEquals(1, lastRender)
                Assertions.assertEquals(1, lastTick)
            }
            opened = true
        }

        ep.open()
        Assertions.assertTrue(opened)
        Assertions.assertEquals(1, lastRender)
        Assertions.assertEquals(0, lastTick)

        schedulerExtractor.getDifferent()!!.run()

        Assertions.assertEquals(1, lastTick)

        ep.close()
        ep.open()

        schedulerExtractor.getDifferent()
        Assertions.assertEquals(2, lastTick)
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun initServer() {
            MinecraftServerMockObj.apply { addExtension(MinecraftItemExtension()) }.apply()
        }

        private const val FIRST_COMPONENT_TICK: Long = 0
    }

}
