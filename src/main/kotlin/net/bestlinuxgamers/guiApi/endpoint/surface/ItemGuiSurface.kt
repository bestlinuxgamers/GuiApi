package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ItemGuiSurface(
    player: Player,
    private val title: String,
    lines: Int,
    eventDispatcher: MinecraftGuiEventDispatcher
) : MinecraftGuiSurface(player, lines, eventDispatcher) {

    init {
        if (lines < 1 || lines > MAX_GUI_SIZE) throw IllegalArgumentException("Guis must have 1-$MAX_GUI_SIZE lines")
    }

    private var inventory: Inventory? = null

    override fun isOpened(): Boolean = inventory != null

    override fun setupSurface(items: Array<ItemStack?>) {
        val inventory = createInventory(items)
        this.inventory = inventory
        player.openInventory(inventory)
    }

    /**
     * Generiert ein neues Inventar nach den Einstellungen der Instanz
     * @param items Items, welche das Inventar beinhalten soll
     */
    private fun createInventory(items: Array<ItemStack?>): Inventory {
        return Bukkit.createInventory(player, lines * GUI_WIDTH, title).apply { writeItems(items) }
    }


    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        inventory?.updateItems(items, lastItems) ?: throw IllegalStateException("Inventory is not initialized")

        player.updateInventory()
    }

    @SurfaceManagerOnly
    override fun close() {
        player.closeInventory()
    }

    override fun getComponentIndex(event: InventoryClickEvent): Int = event.slot

    @EventDispatcherOnly
    override fun performClose() {
        stopListening()
        inventory = null
    }

    companion object {
        const val MAX_GUI_SIZE = 6
    }

}
