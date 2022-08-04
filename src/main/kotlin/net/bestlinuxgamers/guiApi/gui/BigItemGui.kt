package net.bestlinuxgamers.guiApi.gui

import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.EndpointEventDispatcher
import net.bestlinuxgamers.guiApi.endpoint.surface.*
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
) : MinecraftGuiSurface(player, guiSize + playerInventorySize) {

    constructor(player: Player, title: String, additionalLines: Int, eventDispatcher: MinecraftGuiEventDispatcher) :
            this(player, title, ItemGuiSurface.MAX_GUI_SIZE, additionalLines, eventDispatcher)

    @Suppress("unused")
    constructor(player: Player, title: String, eventDispatcher: MinecraftGuiEventDispatcher) :
            this(player, title, PlayerInventorySurface.PLAYER_INV_SIZE, eventDispatcher)


    private val guiSurface = ItemGuiSurface(player, title, guiSize, eventDispatcher).apply {
        registerEndpoint(object : EndpointEventDispatcher {
            @EventDispatcherOnly
            override fun performClick(clickedSlot: Int, event: InventoryClickEvent) {
                this@BigItemGuiSurface.dispatchPerformClickGui(clickedSlot, event)
            }

            @EventDispatcherOnly
            override fun performClose() {
                this@BigItemGuiSurface.dispatchPerformClose()
            }

        })
    }
    private val playerInventorySurface = PlayerInventorySurface(player, playerInventorySize, eventDispatcher).apply {
        registerEndpoint(object : EndpointEventDispatcher {
            @EventDispatcherOnly
            override fun performClick(clickedSlot: Int, event: InventoryClickEvent) {
                this@BigItemGuiSurface.dispatchPerformClickInv(clickedSlot, event)
            }

            @EventDispatcherOnly
            override fun performClose() {
                this@BigItemGuiSurface.dispatchPerformClose()
            }
        })
    }

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

    @Deprecated("This component is not listening", level = DeprecationLevel.ERROR)
    override fun getComponentIndex(event: InventoryClickEvent): Int {
        throw IllegalAccessException("This component is not listening")
    }

    override fun startListening() {
        forAllSurfaces { startListening() }
    }

    override fun stopListening() {
        forAllSurfaces { stopListening() }
    }

    @EventDispatcherOnly
    override fun performClose() {
        forAllSurfaces { performClose() }
    }

    @EventDispatcherOnly
    private fun dispatchPerformClose() {
        TODO("Not yet implemented")
    }

    @EventDispatcherOnly
    private fun dispatchPerformClickGui(clickedSlot: Int, event: InventoryClickEvent) {
        TODO("Not yet implemented")
    }

    @EventDispatcherOnly
    private fun dispatchPerformClickInv(clickedSlot: Int, event: InventoryClickEvent) {
        TODO("Not yet implemented")
    }

    //util

    private fun getGuiItems(items: Array<ItemStack?>) = items.copyOfRange(0, guiSize)

    private fun getPlayerItems(items: Array<ItemStack?>) = items.copyOfRange(guiSize, playerInventorySize)

    private fun forAllSurfaces(action: ListeningMinecraftGuiSurface.() -> Unit) {
        guiSurface.apply(action)
        playerInventorySurface.apply(action)
    }

}
