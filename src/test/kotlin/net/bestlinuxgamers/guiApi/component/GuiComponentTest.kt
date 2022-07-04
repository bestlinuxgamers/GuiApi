package net.bestlinuxgamers.guiApi.component

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GuiComponentTest {

    @Test
    fun testSetup() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), false) {
            override fun setUp() {
                throw TestSucceeded()
            }

            override fun beforeRender(frame: Long) {}
        }

        Assertions.assertThrows(TestSucceeded::class.java) { TestComponent() }
    }

    @Test
    fun testLock() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), false) {
            override fun setUp() {}
            override fun beforeRender(frame: Long) {}
        }

        val instance = TestComponent()
        val instance2 = TestComponent()
        val hooker = TestComponent().apply { lock() }

        Assertions.assertEquals(null, instance.getParentComponent())
        Assertions.assertEquals(null, hooker.getParentComponent())

        hooker.setComponent(instance, 0)

        Assertions.assertThrows(GuiComponent.ComponentAlreadyInUseException::class.java) {
            instance2.setComponent(instance, 0)
        }

        Assertions.assertThrows(GuiComponent.ComponentAlreadyInUseException::class.java) { instance.lock() }

        Assertions.assertThrows(GuiComponent.ComponentAlreadyInUseException::class.java) {
            instance2.setComponent(hooker, 0)
        }


    }

    @Test
    fun testRekursion() {
        class TestComponent : GuiComponent(ReservedSlots(1, 1), true) {
            override fun setUp() {}
            override fun beforeRender(frame: Long) {}
        }

        val instance1 = TestComponent() //hook instance4

        Assertions.assertThrows(GuiComponent.ComponentRekursionException::class.java) {
            instance1.setComponent(instance1, 0)
        }

        val instance2 = TestComponent() //hook instance1
        val instance3 = TestComponent() //hook instance2
        val instance4 = TestComponent() //hooke instance3

        instance1.setComponent(instance2, 0)
        instance2.setComponent(instance3, 0)
        instance3.setComponent(instance4, 0)

        Assertions.assertThrows(GuiComponent.ComponentRekursionException::class.java) {
            instance4.setComponent(instance1, 0)
        }

        val instance5 = TestComponent()
        instance4.setComponent(instance5, 0)
    }

    private class TestSucceeded : Exception()
}
