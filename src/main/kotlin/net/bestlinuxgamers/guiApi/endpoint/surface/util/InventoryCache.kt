package net.bestlinuxgamers.guiApi.endpoint.surface.util

import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class InventoryCache(private val inventory: Inventory) {

    private var cache: Array<ItemStack?> = fetchContents()

    fun saveCache() {
        cache = fetchContents()
    }

    private fun fetchContents(): Array<ItemStack?> = Array(inventory.size) { inventory.getItem(it) }


    fun restoreCache() {
        inventory.writeItems(cache)
    }

    fun getCache() = cache.clone()

}
