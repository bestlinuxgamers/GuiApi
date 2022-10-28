package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.essentials.ItemComponent
import net.bestlinuxgamers.guiApi.component.util.*
import net.bestlinuxgamers.guiApi.templates.server.MinecraftItemExtension
import net.bestlinuxgamers.guiApi.templates.server.MinecraftServerMockObj
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class GuiComponentTest {

    @Test
    fun testLock() {
        val instance = ResizableTestComponent(1, 1)
        val instance2 = ResizableTestComponent(1, 1)
        val hooker = ResizableTestComponent(1, 1).apply { lock() }

        Assertions.assertEquals(null, instance.getParentComponent())
        Assertions.assertEquals(null, hooker.getParentComponent())

        hooker.setComponent(instance, 0)

        Assertions.assertThrows(ComponentAlreadyInUseException::class.java) {
            instance2.setComponent(instance, 0)
        }

        Assertions.assertThrows(ComponentAlreadyInUseException::class.java) { instance.lock() }

        Assertions.assertThrows(ComponentAlreadyInUseException::class.java) {
            instance2.setComponent(hooker, 0)
        }


    }

    @Test
    fun testUnHook() {
        val instance1 = ResizableTestComponent(1, 1)
        val instance2 = ResizableTestComponent(1, 1)
        val comp1 = ResizableTestComponent(1, 1)
        val comp2 = ResizableTestComponent(1, 1)

        instance1.setComponent(comp1, 0)
        instance1.removeComponent(comp1)
        instance2.setComponent(comp1, 0)
        Assertions.assertThrows(ComponentAlreadyInUseException::class.java) { instance1.setComponent(comp1, 0) }
        instance1.setComponent(comp2, 0)
        instance2.removeComponent(comp1)
        instance1.removeComponent(comp2)
        instance1.setComponent(comp1, 0)
        instance2.setComponent(comp2, 0)
    }

    @Test
    fun testRekursion() {
        val instance1 = ResizableTestComponent(1, 1) //hook instance4

        Assertions.assertThrows(ComponentRekursionException::class.java) {
            instance1.setComponent(instance1, 0)
        }

        val instance2 = ResizableTestComponent(1, 1) //hook instance1
        val instance3 = ResizableTestComponent(1, 1) //hook instance2
        val instance4 = ResizableTestComponent(1, 1) //hooke instance3

        instance1.setComponent(instance2, 0)
        instance2.setComponent(instance3, 0)
        instance3.setComponent(instance4, 0)

        Assertions.assertThrows(ComponentRekursionException::class.java) {
            instance4.setComponent(instance1, 0)
        }

        val instance5 = ResizableTestComponent(1, 1)
        instance4.setComponent(instance5, 0)
    }

    @Test
    fun testSetItemSquare() {
        val instance = ResizableTestComponent(4, 4)
        val component2 = ResizableTestComponent(2, 4)

        instance.setComponent(component2, 0)

        Assertions.assertThrows(ComponentAlreadyInUseException::class.java) {
            instance.setComponent(component2, 8)
        }

        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            instance.setComponent(ResizableTestComponent(2, 4), 9)
        }

        Assertions.assertThrows(ComponentOverlapException::class.java) {
            instance.setComponent(ResizableTestComponent(2, 3), 5)
        }

        instance.setComponent(ResizableTestComponent(2, 4), 8)

        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            instance.setComponent(ResizableTestComponent(1, 3), 17)
        }

        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            instance.setComponent(ResizableTestComponent(1, 3), -4)
        }
    }

    @Test
    fun testSetComponent() {
        val reserved = ReservedSlots(
            arrayOf(
                arrayOf(false, true),
                arrayOf(false, true),
                arrayOf(true, false, true),
                emptyArray(),
                arrayOf(false, false, true, false, true),
                arrayOf(true, true, true, true)
            )
        )

        val instance = ResizableTestComponent(10, 10)
        val testCpn = AsymmetricTestComponent(reserved)

        Assertions.assertThrows(SlotNotReservedException::class.java) {
            testCpn.setComponent(ResizableTestComponent(2, 1), 1)
        }

        instance.setComponent(testCpn, 0)
        instance.setComponent(ResizableTestComponent(1, 1), 0)
        instance.setComponent(ResizableTestComponent(1, 1), 2)

        Assertions.assertThrows(ComponentOverlapException::class.java) {
            instance.setComponent(ResizableTestComponent(1, 1), 1)
        }
        instance.setComponent(ResizableTestComponent(1, 1), 10)
        Assertions.assertThrows(ComponentOverlapException::class.java) {
            instance.setComponent(ResizableTestComponent(1, 1), 11)
        }
        instance.setComponent(ResizableTestComponent(1, 8), 12)
        instance.setComponent(ResizableTestComponent(1, 10), 30)

    }

    @Test
    fun testRemoveComponent() {
        val instance = ResizableTestComponent(2, 2)

        val comp2 = ResizableTestComponent(2, 1, renderFallback = ItemStack(Material.STICK))

        instance.setComponent(ResizableTestComponent(2, 1, renderFallback = ItemStack(Material.STONE)), 0)
        instance.setComponent(comp2, 1)

        val target1 = Array(4) {
            when (it) {
                0, 2 -> ItemStack(Material.STONE)
                1, 3 -> ItemStack(Material.STICK)
                else -> ItemStack(Material.BELL)
            }
        }
        val target2 = Array(4) {
            when (it) {
                0, 2 -> ItemStack(Material.STONE)
                1, 3 -> null
                else -> ItemStack(Material.BELL)
            }
        }

        Assertions.assertArrayEquals(target1, instance.render(0))

        instance.removeComponent(comp2)

        Assertions.assertArrayEquals(target2, instance.render(1))
    }

    @Test
    fun testGetComponentsOfType() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), true) {
            override fun setUp() {}
            override fun beforeRender(frame: Long) {}
        }

        val instance = ResizableTestComponent(4, 4)
        val comp1 = ResizableTestComponent(1, 1)
        val comp2 = TestComponent()
        val comp3 = ResizableTestComponent(1, 1)
        val comp4 = TestComponent()

        instance.apply {
            setComponent(comp1, 0)
            setComponent(comp2, 1)
            setComponent(comp3, 2)
            setComponent(comp4, 3)
        }

        Assertions.assertEquals(setOf(comp1, comp3), instance.getComponentsOfType<ResizableTestComponent>())
        Assertions.assertEquals(setOf(comp2, comp4), instance.getComponentsOfType<TestComponent>())

        Assertions.assertEquals(setOf(comp1, comp2, comp3, comp4), instance.getComponents())
    }

    @Test
    fun testGetComponentOfIndex() {
        val reserved = ReservedSlots(
            arrayOf(
                arrayOf(false, true),
                arrayOf(false, true),
                arrayOf(true, false, true),
                emptyArray(),
                arrayOf(false, false, true, false, true),
                arrayOf(true, true, true, true)
            )
        )
        val instance = AsymmetricTestComponent(reserved)

        val comp1 = ResizableTestComponent(2, 1)
        val comp2 = ResizableTestComponent(1, 4)

        instance.setComponent(comp1, 0)
        instance.setComponent(comp2, 6)

        Assertions.assertEquals(comp1, instance.getComponentOfIndex(0))
        Assertions.assertEquals(comp1, instance.getComponentOfIndex(1))
        Assertions.assertEquals(comp2, instance.getComponentOfIndex(6))
        Assertions.assertEquals(comp2, instance.getComponentOfIndex(9))
    }

    @Test
    fun testGetLocalIndexOfComponentIndex() {
        val comp1Reserved = ReservedSlots(
            arrayOf(
                arrayOf(true),
                arrayOf(false, true)
            )
        )
        val comp1 = AsymmetricTestComponent(comp1Reserved)

        val comp2Reserved = ReservedSlots(
            arrayOf(
                arrayOf(false, true),
                arrayOf(true)
            )
        )
        val comp2 = AsymmetricTestComponent(comp2Reserved)

        val instance = ResizableTestComponent(2, 2).apply {
            setComponent(comp1, 0)
            setComponent(comp2, 0)
        }
        Assertions.assertEquals(setOf(0), instance.getLocalIndexOfComponentIndex(comp1, 0))
        Assertions.assertEquals(setOf(3), instance.getLocalIndexOfComponentIndex(comp1, 1))
        Assertions.assertEquals(setOf(1), instance.getLocalIndexOfComponentIndex(comp2, 0))
        Assertions.assertEquals(setOf(2), instance.getLocalIndexOfComponentIndex(comp2, 1))
        Assertions.assertEquals(setOf<Int>(), instance.getLocalIndexOfComponentIndex(comp1, 2))
    }

    @Test
    fun testRenderSquare() {
        val target: Array<ItemStack?> =
            Array(16) { if (it < 8) ItemStack(Material.BARRIER) else ItemStack(Material.BEDROCK) }
        val instance = ResizableTestComponent(4, 4, ItemStack(Material.BEDROCK))
        val comp1 = ResizableTestComponent(2, 4, ItemStack(Material.BARRIER))

        instance.setComponent(comp1, 0)

        val result: Array<ItemStack?> = instance.render(0)

        Assertions.assertArrayEquals(target, result)
    }

    @Test
    fun testRender() {
        val reserved = ReservedSlots(
            arrayOf(
                arrayOf(false, true, true),
                arrayOf(false, false, false, true),
                arrayOf(true, false, false, true)
            )
        )

        val comp2 = ResizableTestComponent(2, 1).apply {
            setComponent(ItemComponent(ItemStack(Material.STICK)), 0)
            setComponent(ItemComponent(ItemStack(Material.EGG)), 1)
        }
        val comp1 = AsymmetricTestComponent(reserved, ItemStack(Material.BARRIER)).apply {
            setComponent(ItemComponent(ItemStack(Material.STONE)), 0)
            setComponent(ItemComponent(ItemStack(Material.COBBLESTONE)), 1)
            setComponent(comp2, 2)
        }
        val instance = ResizableTestComponent(3, 4, renderFallback = ItemStack(Material.BEDROCK)).apply {
            setComponent(comp1, 0)
        }

        val target: Array<ItemStack> = Array(12) {
            when (it) {
                0, in 3..6, 9, 10 -> ItemStack(Material.BEDROCK)
                1 -> ItemStack(Material.STONE)
                2 -> ItemStack(Material.COBBLESTONE)
                7 -> ItemStack(Material.STICK)
                8 -> ItemStack(Material.BARRIER)
                11 -> ItemStack(Material.EGG)
                else -> ItemStack(Material.CAKE)
            }
        }

        Assertions.assertArrayEquals(target, instance.render(0))
    }

    @Test
    fun testStatic() {
        val instance = ResizableTestComponent(2, 2, renderFallback = ItemStack(Material.BEDROCK))
        val comp1 = ResizableTestComponent(1, 2, renderFallback = ItemStack(Material.BARRIER))
        val comp2 = ResizableTestComponent(1, 2, renderFallback = ItemStack(Material.STICK))

        val target: Array<ItemStack> = Array(4) {
            when (it) {
                0, 1 -> ItemStack(Material.BARRIER)
                2, 3 -> ItemStack(Material.BEDROCK)
                else -> ItemStack(Material.BELL)
            }
        }


        instance.setComponent(comp1, 0)
        Assertions.assertArrayEquals(target, instance.renderNextFrame(0))
        instance.setComponent(comp2, 2)
        Assertions.assertArrayEquals(target, instance.renderNextFrame(1))
    }

    @Test
    fun testSetup() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), true, renderFallback = ItemStack(Material.BARRIER)) {
            override fun setUp() {
                setComponent(ItemComponent(ItemStack(Material.STICK)), 0)
            }

            override fun beforeRender(frame: Long) {}
        }

        val instance = TestComponent()
        val target = Array(1) { ItemStack(Material.STICK) }

        Assertions.assertArrayEquals(target, instance.renderNextFrame(0))
    }

    @Test
    fun testBeforeRender() {
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
        val target1 = Array(1) { ItemStack(Material.STICK) }
        val target2 = Array(1) { ItemStack(Material.STONE) }

        instance.setComponent(ResizableTestComponent(1, 1, renderFallback = ItemStack(Material.STICK)), 0)

        Assertions.assertArrayEquals(target1, instance.renderNextFrame(0))
        Assertions.assertArrayEquals(target2, instance.renderNextFrame(1))
    }


    private class ResizableTestComponent(
        height: Int,
        width: Int,
        renderFallback: ItemStack? = null,
        static: Boolean = true
    ) :
        GuiComponent(ReservedSlots(height, width), static, renderFallback = renderFallback) {
        override fun setUp() {}
        override fun beforeRender(frame: Long) {}
    }

    private class AsymmetricTestComponent(reserved: ReservedSlots, renderFallback: ItemStack? = null) :
        GuiComponent(reserved, true, renderFallback = renderFallback) {
        override fun setUp() {}
        override fun beforeRender(frame: Long) {}
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun initServer() {
            MinecraftServerMockObj.apply { addExtension(MinecraftItemExtension()) }.apply()
        }
    }
}
