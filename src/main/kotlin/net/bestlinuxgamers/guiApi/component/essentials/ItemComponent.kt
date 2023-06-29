package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import org.bukkit.inventory.ItemStack

/**
 * Komponente bestehend aus einem Item.
 * @param item Daten des Items
 * @param reservedSlots Oberflächen-Struktur der Komponente
 */
class ItemComponent(private val item: ItemStack, reservedSlots: ReservedSlots) :
    RenderEndpointComponent(item, reservedSlots) {
    constructor(item: ItemStack) : this(item, ReservedSlots(1, 1))
}
