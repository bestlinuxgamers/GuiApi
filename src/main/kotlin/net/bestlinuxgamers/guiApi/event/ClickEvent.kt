package net.bestlinuxgamers.guiApi.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class ClickEvent : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        event.action
    }

}
