package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.ReservedSlots
import org.bukkit.inventory.ItemStack

class ItemComponent(private val item: ItemStack) : GuiComponent(ReservedSlots(1, 1), true) {
    override fun setUp() {}
    override fun render(frame: Long): Array<ItemStack> {
        return Array(1) { item }
    }
}
