package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay.Companion.INVENTORY_WIDTH
import net.bestlinuxgamers.guiApi.endpoint.surface.util.DisplayAlreadyOpenedException
import net.bestlinuxgamers.guiApi.endpoint.surface.util.InventoryCache
import net.bestlinuxgamers.guiApi.event.*
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

/**
 * Die Oberfläche des Minecraft Spieler-Inventars (Hot-bar und Menü mit Tastendruck auf "E").
 * @param player Spieler, dem das Inventar gehört.
 * Bei mehreren Instanzen für den gleichen Spieler können Probleme auftreten!
 */
class PlayerInventoryDisplay(override val player: Player) : MinecraftDisplay {
    //TODO optionale manager Klasse zur vermeidung von doppelten Spieler-Instanzen

    private val inventory: PlayerInventory = player.inventory
    private val inventoryCache = InventoryCache(inventory)
    private var inUse = false

    override val clickEventIdentifier: ClickEventIdentifier = GuiClickEventIdentifier(player, inventory)
    override val closeActionEventIdentifier: CloseEventIdentifier = object : CloseEventIdentifier() {
        override fun isEvent(event: InventoryCloseEvent): Boolean = false
    }
    override val eventRegistrations: Set<EventRegistration<out EventListenerAdapter<out Event>, out Event>> = setOf()

    override val reservedSlots = RESERVED_SLOTS


    @SurfaceManagerOnly
    override fun open(items: Array<ItemStack?>) {
        if (inUse) throw DisplayAlreadyOpenedException()
        inUse = true
        inventoryCache.saveCache()
        inventory.writeItems(convertToPlayerInventoryIndex(items))
    }

    @SurfaceManagerOnly
    override fun isOpened(): Boolean = inUse

    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        inventory.updateItems(
            convertToPlayerInventoryIndex(items),
            lastItems?.let { convertToPlayerInventoryIndex(it) }
        )
    }

    @SurfaceManagerOnly
    override fun close() { //TODO close funktioniert nicht (animation stoppen, listening unregister)
        @OptIn(EventDispatcherOnly::class)
        onClose()
    }

    /**
     * Das Spielerinventar kann durch das Schließ-Event nicht geschlossen werden.
     */
    @EventDispatcherOnly
    override fun onClose() { //TODO bei leaven schließen
        inventoryCache.restoreCache()
        inUse = false
    }

    /**
     * Stellt ein Inventar-Abbild so um, dass es zu dem Index des Spieler-Inventars passt.
     * Die unterste Zeile des Spielerinventars fängt mit dem Index 0-8 an.
     * Anschließend geht es normal in der obersten Zeile weiter (9-17, 18-26, ...).
     * @param items Inventar-Abbild mit normalem Index
     * @return Inventar-Abbild mit Spielerinventar Index
     */
    private fun convertToPlayerInventoryIndex(items: Array<ItemStack?>): Array<ItemStack?> {
        val start = 0
        val end = reservedSlots.totalReserved
        val lastLineStart = end - INVENTORY_WIDTH

        return items.copyOfRange(lastLineStart, end).toMutableList()
            .apply { addAll(items.copyOfRange(start, lastLineStart)) }.toTypedArray()
    }

    @EventDispatcherOnly
    override fun getComponentSlot(event: InventoryClickEvent): Int {
        return if (event.slot in 0 until INVENTORY_WIDTH) {
            reservedSlots.totalReserved - INVENTORY_WIDTH + event.slot
        } else {
            event.slot - INVENTORY_WIDTH
        }
    }

    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        const val PLAYER_INVENTORY_LINES = 4
        val RESERVED_SLOTS = ReservedSlots(PLAYER_INVENTORY_LINES, INVENTORY_WIDTH)
    }
}
