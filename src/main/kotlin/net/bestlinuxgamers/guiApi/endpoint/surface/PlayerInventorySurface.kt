package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.endpoint.surface.util.InventoryCache
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class PlayerInventorySurface(player: Player, lines: Int, eventDispatcher: MinecraftGuiEventDispatcher) :
    MinecraftGuiSurface(player, lines, eventDispatcher) {

    init {
        if (lines < 1 || lines > PLAYER_INV_SIZE) throw IllegalArgumentException("Inventorys must have 1-$PLAYER_INV_SIZE lines")
    }

    private var playerInventory: PlayerInventory? = null
    private var inventoryItems: InventoryCache? = null

    @SurfaceManagerOnly
    override fun isOpened(): Boolean = playerInventory != null

    override fun setupSurface(items: Array<ItemStack?>) {
        val inventory = player.inventory
        playerInventory = inventory
        inventoryItems = InventoryCache(inventory)
        inventory.writeItems(convertWeirdIndex(items))
    }


    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        playerInventory?.updateItems(convertWeirdIndex(items), lastItems?.let { convertWeirdIndex(it) })
            ?: throw IllegalStateException("Inventory is not initialized")
    }

    @SurfaceManagerOnly
    override fun close() {
        player.closeInventory()
    }

    override fun getComponentIndex(event: InventoryClickEvent): Int { //TODO -1 krass unschön
        return if (event.clickedInventory == playerInventory) {
            return if (event.slot in 0 until GUI_WIDTH) {
                surfaceSize - GUI_WIDTH + event.slot
            } else {
                event.slot - GUI_WIDTH
            }
        } else {
            -1
        }
    }

    private fun convertWeirdIndex(items: Array<ItemStack?>): Array<ItemStack?> {
        val start = 0
        val end = surfaceSize
        val lastLineStart = end - GUI_WIDTH

        return items.copyOfRange(lastLineStart, end).toMutableList()
            .apply { addAll(items.copyOfRange(start, lastLineStart)) }.toTypedArray()
    }

    @EventDispatcherOnly
    override fun performClose() {
        inventoryItems?.restoreCache()
        player.updateInventory()

        inventoryItems = null
        playerInventory = null
    }

    companion object {
        const val PLAYER_INV_SIZE = 4
    }

}
