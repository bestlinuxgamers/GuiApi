package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.ItemGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.PlayerInventorySurface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.event.MinecraftGuiEventDispatcher
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

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
    title: String,
    private val guiSize: Int,
    private val playerInventorySize: Int,
    eventDispatcher: MinecraftGuiEventDispatcher
) : MinecraftGuiSurface(player, guiSize + playerInventorySize, eventDispatcher) {

    constructor(player: Player, title: String, additionalLines: Int, eventDispatcher: MinecraftGuiEventDispatcher) :
            this(player, title, ItemGuiSurface.MAX_GUI_SIZE, additionalLines, eventDispatcher)

    @Suppress("unused")
    constructor(player: Player, title: String, eventDispatcher: MinecraftGuiEventDispatcher) :
            this(player, title, PlayerInventorySurface.PLAYER_INV_SIZE, eventDispatcher)


    private val guiSurface = ItemGuiSurface(player, title, guiSize, eventDispatcher)
    private val playerInventorySurface = PlayerInventorySurface(player, playerInventorySize, eventDispatcher)

    override fun isOpened(): Boolean = guiSurface.isOpened()

    override fun setupSurface(items: Array<ItemStack?>) {
        playerInventorySurface.setupSurface(getPlayerItems(items))
        guiSurface.setupSurface(getGuiItems(items))
    }

    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        guiSurface.updateItems(getGuiItems(items), lastItems?.let { getGuiItems(it) })
        playerInventorySurface.updateItems(
            getPlayerItems(items),
            lastItems?.let { getPlayerItems(it) }
        )
    }

    @SurfaceManagerOnly
    override fun close() = guiSurface.close()


    override fun getComponentIndex(event: InventoryClickEvent): Int {
        TODO("Not yet implemented")
    }

    @EventDispatcherOnly
    override fun performClose() {
        TODO("Not yet implemented")
    }

    private fun getGuiItems(items: Array<ItemStack?>) = items.copyOfRange(0, guiSize)

    private fun getPlayerItems(items: Array<ItemStack?>) = items.copyOfRange(guiSize, playerInventorySize)

}
