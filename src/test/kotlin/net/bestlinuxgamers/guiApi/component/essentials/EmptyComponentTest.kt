package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.RenderOnly
import net.bestlinuxgamers.guiApi.component.util.ComponentOverlapException
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.test.templates.server.MinecraftItemExtension
import net.bestlinuxgamers.guiApi.test.templates.server.MinecraftServerMockObj
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class EmptyComponentTest {

    @Test
    fun testEmptyReserved() {
        val testComp = ResizableTestComponent(2, 2).apply {
            setComponent(ResizableTestComponent(2, 1, renderFallback = ItemStack(Material.STONE)), 0)
            setComponent(EmptyComponent(ReservedSlots(2, 1)), 1)
        }

        val template = arrayOf(ItemStack(Material.STONE), null, ItemStack(Material.STONE), null)

        @OptIn(RenderOnly::class)
        Assertions.assertArrayEquals(template, testComp.renderNextFrame(0))
    }

    @Test
    fun testEmptySingle() {
        val testComp = ResizableTestComponent(2, 2).apply {
            setComponent(ResizableTestComponent(2, 1, renderFallback = ItemStack(Material.STONE)), 0)
            setComponent(EmptyComponent(), 1)
            setComponent(ResizableTestComponent(1, 1, renderFallback = ItemStack(Material.STONE)), 3)
        }

        val template = arrayOf(ItemStack(Material.STONE), null, ItemStack(Material.STONE), ItemStack(Material.STONE))

        @OptIn(RenderOnly::class)
        Assertions.assertArrayEquals(template, testComp.renderNextFrame(0))
    }

    @Test
    fun testOverlap() {
        val testComp = ResizableTestComponent(1, 2, renderFallback = ItemStack(Material.STONE)).apply {
            setComponent(ResizableTestComponent(1, 1), 0)
            setComponent(EmptyComponent(), 1)
        }

        Assertions.assertThrows(ComponentOverlapException::class.java) {
            testComp.setComponent(ResizableTestComponent(1, 1), 1)
        }

    }

    private class ResizableTestComponent(
        height: Int,
        width: Int,
        renderFallback: ItemStack? = null,
        static: Boolean = true,
        smartRender: Boolean = true
    ) : GuiComponent(
        ReservedSlots(height, width),
        static = static,
        smartRender = smartRender,
        renderFallback = renderFallback
    ) {
        override fun beforeRender(frame: Long) {}
        override fun onComponentTick(tick: Long, frame: Long) {}
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun initServer() {
            MinecraftServerMockObj.apply { addExtension(MinecraftItemExtension()) }.apply()
        }
    }

}
