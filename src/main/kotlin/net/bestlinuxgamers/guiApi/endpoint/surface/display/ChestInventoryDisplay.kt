package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.endpoint.surface.display.MinecraftDisplay.Companion.INVENTORY_WIDTH
import net.bestlinuxgamers.guiApi.endpoint.surface.util.DisplayAlreadyOpenedException
import net.bestlinuxgamers.guiApi.event.*
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryAction
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
    disableOtherInventories: Boolean = false,
    unsafeLines: Boolean = false
) : MinecraftDisplay {

    init {
        if (lines < MIN_CHEST_LINES || (lines > MAX_CHEST_LINES && !unsafeLines)) {
            throw IllegalArgumentException("ChestInventoryDisplays must have $MIN_CHEST_LINES-$MAX_CHEST_LINES lines")
        }
    }

    private val inventory: Inventory = Bukkit.createInventory(player, lines * INVENTORY_WIDTH, title)
    private var opened = false

    override val clickEventIdentifier: ClickEventIdentifier = GuiClickEventIdentifier(player, inventory)
    override val externalGuiModificationCancelRegistrations: Set<EventRegistration<out EventListenerAdapter<out Event>, out Event>> =
        if (!disableOtherInventories) setOf(
            EventRegistration(
                LambdaEventIdentifier {
                    // checking if the raw slot is lower than the inventory size to determine that the upper inventory is clicked,
                    // is used because low minecraft versions (like 1.12) do not have InventoryView#getInventory(int) to
                    // determine the affected inventory.
                    if (it.view.topInventory == inventory && it.rawSlots.any { rs -> rs < inventory.size }) {
                        return@LambdaEventIdentifier true
                    }
                    return@LambdaEventIdentifier false
                },
                LambdaEventAction {
                    it.isCancelled = true
                    it.result = Event.Result.DENY
                },
                ItemDragEventListenerAdapter::class
            ),
            EventRegistration(
                LambdaEventIdentifier {
                    return@LambdaEventIdentifier it.whoClicked == player && it.view.topInventory == inventory &&
                            (it.action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                                    it.action == InventoryAction.COLLECT_TO_CURSOR)
                },
                LambdaEventAction {
                    it.isCancelled = true
                },
                ClickEventListenerAdapter::class
            )
        ) else setOf(
            EventRegistration(
                LambdaEventIdentifier {
                    return@LambdaEventIdentifier it.whoClicked == player && it.clickedInventory != inventory
                },
                LambdaEventAction {
                    it.isCancelled = true
                },
                ClickEventListenerAdapter::class
            )
        )
    override val additionalRegistrations: Set<EventRegistration<out EventListenerAdapter<out Event>, out Event>> =
        setOf()

    override fun generateCloseActionRegistration(action: () -> Unit): EventRegistration<out EventListenerAdapter<out Event>, out Event> =
        CloseEventRegistration(GuiCloseEventIdentifier(player, inventory), LambdaEventAction { action() })

    override val reservedSlots = ReservedSlots(lines, INVENTORY_WIDTH)

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
