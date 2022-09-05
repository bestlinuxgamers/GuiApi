package net.bestlinuxgamers.guiApi.endpoint.surface.util

import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Klasse zum speichern und wiederherstellen von Inventar-Items.
 * @param inventory Inventar, dessen Items gespeichert werden sollen
 * @param autoCache Ob die Items des Inventars bereits beim Initialisieren gespeichert werden sollen
 */
class InventoryCache(private val inventory: Inventory, autoCache: Boolean = false) {

    private var cache: Array<ItemStack?> = if (!autoCache) emptyArray() else fetchContents()

    /**
     * Speichert die aktuellen Items des Inventars
     */
    fun saveCache() {
        cache = fetchContents()
    }

    /**
     * Speichert die Items aus dem Inventar in ein Array
     * @return Items des Inventars
     */
    private fun fetchContents(): Array<ItemStack?> = Array(inventory.size) { inventory.getItem(it) }

    /**
     * Schreibt den aktuellen Speicherstand der Items in das Inventar
     * @see getCache
     */
    fun restoreCache() {
        inventory.writeItems(cache)
    }

    /**
     * @return Aktueller Speicherstand der Items
     */
    fun getCache() = cache.clone()

}
