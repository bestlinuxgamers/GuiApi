package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import org.bukkit.inventory.ItemStack

/**
 * Ein einzelnes Item
 * @param item Daten des Items
 */
class ItemComponent(private val item: ItemStack) : RenderEndpointComponent(item, ReservedSlots(1, 1))
