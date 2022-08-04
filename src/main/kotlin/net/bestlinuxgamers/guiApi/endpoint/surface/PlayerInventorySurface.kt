package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class PlayerInventorySurface(player: Player, lines: Int, eventDispatcher: MinecraftGuiEventDispatcher) :
    ListeningMinecraftGuiSurface(player, lines, eventDispatcher) {

    init {
        if (lines < 1 || lines > PLAYER_INV_SIZE) throw IllegalArgumentException("Inventorys must have 1-$PLAYER_INV_SIZE lines")
    }

    override var inventory: Inventory? = null //PlayerInventory
    private var inventoryItems: Array<ItemStack?>? = null

    override fun isOpened(): Boolean = inventory != null

    override fun setupSurface(items: Array<ItemStack?>) {
        val inventory = player.inventory
        this.inventory = inventory
        inventoryItems = Array(inventory.size) { inventory.getItem(it) }
        inventory.writeItems(items)
    }


    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        inventory?.updateItems(items, lastItems) ?: throw IllegalStateException("Inventory is not initialized")
    }

    @SurfaceManagerOnly
    override fun close() {
        player.closeInventory()
    }

    override fun getComponentIndex(event: InventoryClickEvent): Int = event.slot

    @EventDispatcherOnly
    override fun performClose() {
        inventoryItems?.let { inventory?.writeItems(it) }

        inventoryItems = null
        inventory = null
    }

    companion object {
        const val PLAYER_INV_SIZE = 4
    }

}
