package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay.Companion.INVENTORY_WIDTH
import net.bestlinuxgamers.guiApi.endpoint.surface.util.DisplayAlreadyOpenedException
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.EventIdentifier
import net.bestlinuxgamers.guiApi.event.InventoryEventIdentifier
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Die Oberfläche eines Minecraft-Kisten-Inventars
 * @param player Spieler, dem das Inventar gehört
 * @param title Titel des Inventars
 * @param lines Zeilen des Inventars (von 1 - 6)
 * @param unsafeLines Ob es möglich ist mehr als 6 Zeilen zu setzen.
 * Dadurch kann ein verbuggtes, aber größeres Inventar erzeugt werden.
 */
class ChestInventoryDisplay(
    override val player: Player,
    title: String,
    lines: Int,
    unsafeLines: Boolean = false
) : MinecraftDisplay {

    init {
        if (lines < MIN_CHEST_LINES || if (!unsafeLines) lines > MAX_CHEST_LINES else false) {
            throw IllegalArgumentException("ChestInventoryDisplays must have $MIN_CHEST_LINES-$MAX_CHEST_LINES lines")
        }
    }

    private val inventory: Inventory = Bukkit.createInventory(player, lines * INVENTORY_WIDTH, title)
    private var opened = false

    override val reservedSlots = ReservedSlots(lines, INVENTORY_WIDTH)

    override val eventIdentifier: EventIdentifier = InventoryEventIdentifier(inventory)

    @SurfaceManagerOnly
    override fun open(items: Array<ItemStack?>) {
        if (opened) throw DisplayAlreadyOpenedException()
        opened = true
        inventory.writeItems(items)
        player.openInventory(inventory)
    }

    @SurfaceManagerOnly
    override fun isOpened(): Boolean = opened

    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        inventory.updateItems(items, lastItems)
        player.updateInventory()
    }

    @SurfaceManagerOnly
    override fun close() {
        player.closeInventory()
    }

    @EventDispatcherOnly
    override fun onClose() {
        opened = false
    }

    @EventDispatcherOnly
    override fun getComponentSlot(event: InventoryClickEvent) = event.slot

    companion object {
        const val MAX_CHEST_LINES = 6
        const val MIN_CHEST_LINES = 1
    }

}
