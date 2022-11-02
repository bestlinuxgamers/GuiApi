package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.RenderOnly
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import org.bukkit.inventory.ItemStack

/**
 * Ein einzelnes Item
 * @param item Daten des Items
 */
class ItemComponent(private val item: ItemStack) :
    GuiComponent(ReservedSlots(1, 1), true, false, null) {
    override fun setUp() {}
    override fun beforeRender(frame: Long) {}
    override fun onRenderTick(tick: Long, frame: Long) {}

    @RenderOnly
    override fun render(frame: Long): Array<ItemStack?> {
        return Array(1) { item }
    }
}
