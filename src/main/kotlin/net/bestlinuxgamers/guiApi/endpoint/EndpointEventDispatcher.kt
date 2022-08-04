package net.bestlinuxgamers.guiApi.endpoint

import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import org.bukkit.event.inventory.InventoryClickEvent

interface EndpointEventDispatcher { //TODO evtl. generelles Interface für alle Dispatcher der Pipeline

    @EventDispatcherOnly
    fun performClick(clickedSlot: Int, event: InventoryClickEvent)

    @EventDispatcherOnly
    fun performClose()
}
