package net.bestlinuxgamers.guiApi.endpoint.surface.display

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Schnittstelle einer grafischen Benutzeroberfläche
 */
interface DisplayInterface {
    val reservedSlots: ReservedSlots

    /**
     * Öffnet die Oberfläche
     * @param items Items, welche die Oberfläche initial beinhalten soll
     */
    @SurfaceManagerOnly
    fun open(items: Array<ItemStack?>)

    /**
     * @return Ob die Oberfläche geöffnet ist
     */
    @SurfaceManagerOnly
    fun isOpened(): Boolean

    /**
     * Aktualisiert die Items der Oberfläche und überschreibt dabei nur Items, welche sich verändert haben
     * @param items Items, welche das Inventar beinhalten soll
     * @param lastItems aktuelle Items, mit denen auf Veränderungen geprüft werden soll
     * @see [net.bestlinuxgamers.guiApi.extensions.updateItems]
     */
    @SurfaceManagerOnly
    fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?)

    /**
     * Initiiert die Schließung der Oberfläche
     */
    @SurfaceManagerOnly
    fun close()

    //Event

    /**
     * Führt alle Aktionen nach einer Schließung der Oberfläche aus.
     * Sollte durch ein Event eines EventDispatchers aufgerufen werden.
     */
    @EventDispatcherOnly
    fun onClose()

    /**
     * Teilweise lässt sich der im [InventoryClickEvent] angegebene geklickte [InventoryClickEvent.getSlot]
     * nicht 1 zu 1 auf eine [net.bestlinuxgamers.guiApi.component.GuiComponent] anwenden.
     * Diese Methode rechnet den im Event angegeben Slot auf einen Komponenten-Slot um.
     * @param event Event, dessen Slot umgerechnet werden soll
     * @return Der umgerechnete Slot
     */
    @EventDispatcherOnly
    fun getComponentSlot(event: InventoryClickEvent): Int
}
