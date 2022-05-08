package net.bestlinuxgamers.guiApi

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.ReservedSlots
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

abstract class ItemGui(
    private val player: Player,
    private val title: String,
    private val lines: Int,
    private val plugin: JavaPlugin
) :
    GuiComponent(ReservedSlots(lines, 9)) {
    init {
        if (lines < 1 || lines > 6) throw IllegalArgumentException("Guis must have 1-6 lines")
    }

    var inventory: Inventory? = null
    var frameCount: Long = 0
    var scheduler: BukkitTask? = null

    fun open() {
        if (inventory != null) return
        player.openInventory(setInventory(renderNext()))
        startUpdateScheduler()
    }

    private fun renderNext() = renderNextFrame(frameCount++)

    private fun updateInventory(items: Array<ItemStack>) {
        val inventory = this.inventory ?: setInventory(items).also { return }
        items.forEachIndexed { index, item ->
            if (inventory.getItem(index) != item) {
                inventory.setItem(index, item)
            }
        }
        player.updateInventory()
    }

    private fun setInventory(items: Array<ItemStack>): Inventory {
        val inventory = Bukkit.createInventory(player, lines * 9, title)

        items.forEachIndexed { index, item -> inventory.setItem(index, item) }

        this.inventory = inventory
        return inventory
    }

    private fun performUpdateTick() {
        updateInventory(renderNext())
    }

    private fun startUpdateScheduler() {
        if (scheduler != null) return

        scheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, {
            performUpdateTick()
        } as Runnable, 20, 20)
    }

}
