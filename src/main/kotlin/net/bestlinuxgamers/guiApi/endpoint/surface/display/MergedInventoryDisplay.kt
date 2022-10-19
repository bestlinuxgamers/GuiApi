package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.event.*
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Die Oberfläche eines [ChestInventoryDisplay] und des darunter liegenden [PlayerInventoryDisplay]
 * @param player Spieler, dem das Inventar gehört
 * @param chestTitle Titel des [ChestInventoryDisplay]
 * @param chestLines Zeilen des [ChestInventoryDisplay] (1 - 6)
 */
class MergedInventoryDisplay(override val player: Player, chestTitle: String, chestLines: Int) : MinecraftDisplay {

    private val chestInventoryDisplay = ChestInventoryDisplay(player, chestTitle, chestLines)
    private val playerInventoryDisplay = PlayerInventoryDisplay(player)

    override val clickEventIdentifier: EventIdentifier<ClickEventListenerAdapter, InventoryClickEvent> =
        MergedIdentifiersIdentifier(
            setOf(chestInventoryDisplay.clickEventIdentifier, playerInventoryDisplay.clickEventIdentifier)
        )
    override val closeActionEventIdentifier: CloseEventIdentifier = chestInventoryDisplay.closeActionEventIdentifier
    override val eventRegistrations: Set<EventRegistration<out EventListenerAdapter<out Event>, out Event>> =
        chestInventoryDisplay.eventRegistrations + playerInventoryDisplay.eventRegistrations

    override val reservedSlots: ReservedSlots = ReservedSlots(
        chestInventoryDisplay.reservedSlots.getArr2D() + playerInventoryDisplay.reservedSlots.getArr2D()
    )

    @SurfaceManagerOnly
    override fun open(items: Array<ItemStack?>) {
        chestInventoryDisplay.open(getChestItems(items))
        playerInventoryDisplay.open(getPlayerItems(items))
    }

    @SurfaceManagerOnly
    override fun isOpened(): Boolean = chestInventoryDisplay.isOpened()

    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        chestInventoryDisplay.updateItems(getChestItems(items), lastItems?.let { getChestItems(it) })
        playerInventoryDisplay.updateItems(getPlayerItems(items), lastItems?.let { getPlayerItems(it) })
    }

    @SurfaceManagerOnly
    override fun close() = chestInventoryDisplay.close()


    @EventDispatcherOnly
    override fun onClose() {
        chestInventoryDisplay.onClose()
        playerInventoryDisplay.onClose()
    }

    /**
     * @throws IllegalArgumentException Wenn das angegebene Event nicht zu dem Display gehört
     */
    @EventDispatcherOnly
    override fun getComponentSlot(event: InventoryClickEvent): Int {
        return if (chestInventoryDisplay.clickEventIdentifier.isEvent(event)) {
            chestInventoryDisplay.getComponentSlot(event)
        } else if (playerInventoryDisplay.clickEventIdentifier.isEvent(event)) {
            chestInventoryDisplay.reservedSlots.totalReserved + playerInventoryDisplay.getComponentSlot(event)
        } else {
            throw IllegalArgumentException("Event is not associated with this Display")
        }
    }

    /**
     * Extrahiert aus einem zusammengeführten Inventar-Abbild die Items des Kisten-Inventars
     * @return Auf das Kisten-Inventar passendes Extrakt
     */
    private fun getChestItems(items: Array<ItemStack?>) =
        items.copyOfRange(0, chestInventoryDisplay.reservedSlots.totalReserved)

    /**
     * Extrahiert aus einem zusammengeführten Inventar-Abbild die Items des Spieler-Inventars
     * @return Auf das Spielerinventar passendes Extrakt
     */
    private fun getPlayerItems(items: Array<ItemStack?>): Array<ItemStack?> = items.copyOfRange(
        chestInventoryDisplay.reservedSlots.totalReserved,
        chestInventoryDisplay.reservedSlots.totalReserved + playerInventoryDisplay.reservedSlots.totalReserved
    )

}
