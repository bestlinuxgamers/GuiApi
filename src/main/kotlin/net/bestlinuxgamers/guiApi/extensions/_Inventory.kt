package net.bestlinuxgamers.guiApi.extensions

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Schreibt items ins Inventar
 * @param items
 */
fun Inventory.writeItems(items: Array<ItemStack?>) {
    items.forEachIndexed { index, item -> setItem(index, item) }
}

/**
 * Aktualisiert Items, welche sich verändert haben.
 * @param items Gewünschtes Inventar
 * @param comparison Inventar, mit dem [items] auf Änderungen verglichen wird.
 * Wenn Null wird das aktuelle Inventar verwendet.
 */
fun Inventory.updateItems(items: Array<ItemStack?>, comparison: Array<ItemStack?>? = null) {
    val getItemLambda: (Int) -> ItemStack? = if (comparison != null) { it -> comparison[it] } else { it -> getItem(it) }

    items.forEachIndexed { index, item ->
        if (getItemLambda(index) != item) {
            setItem(index, item)
        }
    }
}
