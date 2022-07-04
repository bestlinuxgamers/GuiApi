package net.bestlinuxgamers.guiApi

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.ReservedSlots
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

/**
 * Repräsentiert ein GUI aus Minecraft Items.
 *
 * @param player Spieler, dem das GUI angezeigt werden soll
 * @param title Titel des GUIs
 * @param lines Zeilenanzahl des GUIs
 * @param plugin Plugin, auf welches der Scheduler registriert werden soll
 * @param tickSpeed Schnelligkeit der GUI Updates in Minecraft Ticks
 * @param background Items in Slots, auf denen keine Komponente liegt
 */
abstract class ItemGui(
    private val player: Player,
    private val title: String,
    private val lines: Int,
    private val plugin: JavaPlugin, //TODO eventuell nullable machen und bei null alles static
    private val tickSpeed: Long = 20,
    static: Boolean = false,
    removable: Boolean = false,
    background: ItemStack? = null
) : GuiComponent(ReservedSlots(lines, GUI_WIDTH), static, removable, background) {

    init {
        if (lines < 1 || lines > 6) throw IllegalArgumentException("Guis must have 1-6 lines")
        super.lock()
    }

    private var inventory: Inventory? = null
    private var frameCount: Long = 0
    private var scheduler: BukkitTask? = null

    /**
     * Öffnet das Inventar für den Spieler und startet alle Animationen
     */
    @Suppress("unused")
    fun open() {
        if (inventory != null) return
        player.openInventory(setInventory(renderNext()))
        startUpdateScheduler()
    }

    //TODO close/stop

    /**
     * Rendert das nächste Bild
     * @see [renderNextFrame]
     */
    private fun renderNext() = renderNextFrame(frameCount++)

    /**
     * Aktualisiert das Inventar. Nur Items, welche sich verändert haben, werden verändert.
     * @param items Items, welche das Inventar beinhalten soll
     */
    private fun updateInventory(items: Array<ItemStack?>) {
        val inventory = this.inventory ?: throw IllegalStateException("Inventory is not initialized") //TODO evtl. stop/close

        inventory.updateItems(items, super.getLastRender())

        player.updateInventory()
    }

    /**
     * Erstellt ein neues Inventar und setzt dieses
     * @param items Items, welche das Inventar beinhalten soll
     * @see createInventory
     */
    private fun setInventory(items: Array<ItemStack?>): Inventory {
        if (inventory == null) throw IllegalStateException("Inventory already set")
        return createInventory(items).also { inventory = it }
    }

    /**
     * Generiert ein neues Inventar nach den Einstellungen der Instanz
     * @param items Items, welche das Inventar beinhalten soll
     */
    private fun createInventory(items: Array<ItemStack?>): Inventory {
        val inventory = Bukkit.createInventory(player, lines * GUI_WIDTH, title)

        inventory.writeItems(items)

        return inventory
    }

    /**
     * Führt den nächsten update Tick aus
     */
    private fun performUpdateTick() {
        updateInventory(renderNext()) //TODO was, wenn render länger, als tickSpeed benötigt
    }

    /**
     * Startet den update Tick scheduler
     */
    private fun startUpdateScheduler() {
        if ((scheduler != null) || super.static) return

        scheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, {
            performUpdateTick()
        } as Runnable, tickSpeed, tickSpeed)
    }

    companion object {
        const val GUI_WIDTH = 9
    }

}
