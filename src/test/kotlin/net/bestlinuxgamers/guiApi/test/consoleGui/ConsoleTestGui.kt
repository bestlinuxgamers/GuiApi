package net.bestlinuxgamers.guiApi.test.consoleGui

import net.bestlinuxgamers.guiApi.component.essentials.ItemComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun main() {
    ConsoleTestGui().open()
}

internal class ConsoleTestGui : ConsoleGui(6, 9, tickSpeed = 1, debug = true) {

    override fun beforeRender(frame: Long) {
        println("BEFORE-RENDER FRAME: $frame")
    }

    override fun onComponentTick(tick: Long, frame: Long) {
        println("COMPONENT-TICK: $tick, FRAME: $frame")

        // Calculate the next slot to fill with a stick
        val slot = tick.toInt() % reservedSlots.totalReserved

        // If there is not already a stick in the slot
        if (getComponentOfIndex(slot) == null) {
            // Put a new stick in the next slot
            setComponent(getRemovableStick(), slot)
        }
    }

    /**
     * Creates a stick component, which will be removed by a click.
     */
    private fun getRemovableStick(): ItemComponent {
        val component = ItemComponent(ItemStack(Material.STICK))

        component.setClickable { _, _ ->
            // Remove self from parent component
            removeComponent(component)
        }

        return component
    }

}
