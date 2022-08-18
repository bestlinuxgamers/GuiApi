package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.ItemGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.PlayerInventorySurface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.endpoint.surface.util.InventoryCache
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

abstract class BigItemGui(
    player: Player,
    title: String,
    additionalLines: Int,
    eventDispatcher: MinecraftGuiEventDispatcher,
    schedulerProvider: SchedulerProvider,
    tickSpeed: Long = 20,
    static: Boolean = false,
    background: ItemStack? = null
) : ComponentEndpoint(
    BigItemGuiSurface(player, title, additionalLines, eventDispatcher),
    schedulerProvider,
    tickSpeed,
    static,
    background
)

class BigItemGuiSurface(
    player: Player,
    private val title: String,
    private val guiLines: Int,
    private val playerInventoryLines: Int,
    eventDispatcher: MinecraftGuiEventDispatcher
) : MinecraftGuiSurface(
    player,
    guiLines + playerInventoryLines,
    eventDispatcher
) { //TODO sehr unschön wegen duplicated code & mehr

    constructor(player: Player, title: String, additionalLines: Int, eventDispatcher: MinecraftGuiEventDispatcher) :
            this(player, title, ItemGuiSurface.MAX_GUI_SIZE, additionalLines, eventDispatcher)

    @Suppress("unused")
    constructor(player: Player, title: String, eventDispatcher: MinecraftGuiEventDispatcher) :
            this(player, title, PlayerInventorySurface.PLAYER_INV_SIZE, eventDispatcher)


    private val guiSize by lazy { guiLines * GUI_WIDTH }
    private val playerInventorySize by lazy { playerInventoryLines * GUI_WIDTH }

    private var inv: BigInventory? = null
    private var playerItemCache: InventoryCache? = null


    @SurfaceManagerOnly
    override fun isOpened(): Boolean = inv != null

    override fun setupSurface(items: Array<ItemStack?>) {

        inv = BigInventory(
            Bukkit.createInventory(player, guiLines * GUI_WIDTH, title)
                .also { it.writeItems(getGuiItems(items)) }
                .also { player.openInventory(it) },
            player.inventory
                .also { playerItemCache = InventoryCache(it) }
                .also { it.writeItems(getPlayerItems(items)) }
        )
    }

    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        inv?.let {
            it.gui.updateItems(getGuiItems(items), lastItems?.let { it2 -> getGuiItems(it2) })
            it.playerInventory.updateItems(getPlayerItems(items), lastItems?.let { it2 -> getPlayerItems(it2) })
        }
    }

    @SurfaceManagerOnly
    override fun close() = player.closeInventory()


    override fun getComponentIndex(event: InventoryClickEvent): Int { //TODO super unschön, checks sollten definitiv früher stattfinden
        inv?.let {
            return when (event.clickedInventory) {
                it.gui -> event.slot
                it.playerInventory -> {
                    return if (event.slot in 0 until GUI_WIDTH) {
                        guiSize + playerInventorySize - GUI_WIDTH + event.slot
                    } else {
                        guiSize + event.slot - GUI_WIDTH
                    }
                }

                else -> throw IllegalAccessException("Event not associated with this surface!")
            }
        }
        throw IllegalAccessException("Inventory is not opened!")
    }

    @EventDispatcherOnly
    override fun performClose() {
        playerItemCache?.restoreCache()
    }

    private fun getGuiItems(items: Array<ItemStack?>) = items.copyOfRange(0, guiSize)

    private fun getPlayerItems(items: Array<ItemStack?>): Array<ItemStack?> {
        val start = guiSize
        val end = guiSize + playerInventorySize
        val lastLineStart = end - GUI_WIDTH

        return items.copyOfRange(lastLineStart, end).toMutableList()
            .apply { addAll(items.copyOfRange(start, lastLineStart)) }.toTypedArray()
    }


    class BigInventory(
        val gui: Inventory,
        val playerInventory: PlayerInventory
    )

}
