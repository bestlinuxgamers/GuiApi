package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.util.ComponentAlreadyInUseException
import net.bestlinuxgamers.guiApi.component.util.ComponentOverlapException
import net.bestlinuxgamers.guiApi.component.util.ComponentRekursionException
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GuiComponentTest {

    @Test
    fun testSetup() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), true) {
            override fun setUp() {
                throw TestSucceeded()
            }

            override fun beforeRender(frame: Long) {}
        }

        Assertions.assertThrows(TestSucceeded::class.java) { TestComponent() }
    }

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
    fun testSetItem() {
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
    fun testGetComponentsOfType() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), false) {
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


    private class ResizableTestComponent(height: Int, width: Int) :
        GuiComponent(ReservedSlots(height, width), false) {
        override fun setUp() {}
        override fun beforeRender(frame: Long) {}
    }

    private class AsymmetricTestComponent(reserved: ReservedSlots) :
        GuiComponent(reserved, false) {
        override fun setUp() {}
        override fun beforeRender(frame: Long) {}
    }

    private class TestSucceeded : Exception()
}
