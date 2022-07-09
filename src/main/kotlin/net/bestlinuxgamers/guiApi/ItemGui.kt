package net.bestlinuxgamers.guiApi

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.extensions.updateItems
import net.bestlinuxgamers.guiApi.extensions.writeItems
import net.bestlinuxgamers.guiApi.handler.ItemGuiHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

/**
 * Repräsentiert ein GUI aus Minecraft Items.
 *
 * @param player Spieler, dem das GUI angezeigt werden soll
 * @param title Titel des GUIs
 * @param lines Zeilenanzahl des GUIs
 * @param tickSpeed Schnelligkeit der GUI Updates in Minecraft Ticks
 * @param handler Handler zum Verwalten von Events und zum Starten des render schedulers.
 * Wenn null ist das GUI automatisch statisch!
 * @param background Items in Slots, auf denen keine Komponente liegt
 * @see ItemGuiHandler
 */
abstract class ItemGui(
    private val player: Player,
    private val title: String,
    private val lines: Int,
    private val tickSpeed: Long = 20,
    private val handler: ItemGuiHandler?,
    static: Boolean = false,
    removable: Boolean = false,
    background: ItemStack? = null
) : GuiComponent(ReservedSlots(lines, GUI_WIDTH), (static || handler == null), removable, background) {

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
        if (!static) handler?.registerListeningGui(player, this)

        player.openInventory(setInventory(renderNext()))
        startUpdateScheduler()
    }

    /**
     * Schließt das Inventar für den Spieler
     */
    @Suppress("unused")
    fun close() {
        player.closeInventory()
    }

    /**
     * Führt die Schließ-Routine durch.
     * Sollte nur von einem close Event im [handler] aufgerufen werden!
     * @see ItemGuiHandler.dispatchCloseEvent
     */
    internal fun performClose() {
        handler?.unregisterListeningGui(player)
        stopUpdateScheduler()
        inventory = null
        frameCount = 0
        //TODO evtl. open onClose() in GuiComponent
    }

    /**
     * Führt die Klick-Routine durch.
     * Sollte nur von einem klick Event im [handler] aufgerufen werden!
     * @see ItemGuiHandler.dispatchClickEvent
     */
    internal fun performClick(event: InventoryClickEvent) {
        event.isCancelled = true
        click(event, event.slot)
    }

    //render

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
        val inventory =
            this.inventory ?: throw IllegalStateException("Inventory is not initialized") //TODO evtl. stop/close

        inventory.updateItems(items, super.getLastRender())

        player.updateInventory()
    }

    //Inventory

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

    //update scheduler

    /**
     * Führt den nächsten update Tick aus
     */
    private fun performUpdateTick() {
        updateInventory(renderNext()) //TODO was, wenn render länger, als tickSpeed benötigt //TODO Items sync updaten
    }

    /**
     * Startet den update Tick scheduler
     */
    private fun startUpdateScheduler() {
        if ((scheduler != null) || super.static) return
        if (handler == null) return

        scheduler = handler.runTaskTimerAsynchronously(tickSpeed, tickSpeed) { performUpdateTick() }
    }

    /**
     * Stoppt den update Tick scheduler
     * @see startUpdateScheduler
     */
    private fun stopUpdateScheduler() {
        if (!static) {
            scheduler?.cancel().also { scheduler = null }
        }
    }

    companion object {
        const val GUI_WIDTH = 9
    }

}
