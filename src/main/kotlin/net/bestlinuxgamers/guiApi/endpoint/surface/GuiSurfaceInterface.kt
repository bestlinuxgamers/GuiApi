package net.bestlinuxgamers.guiApi.endpoint.surface

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import org.bukkit.inventory.ItemStack

/**
 * Diese Schnittstelle beinhaltet alle Funktionen, um mit einer grafischen Oberfläche zu kommunizieren und
 * sich für Events zu registrieren.
 * @see [net.bestlinuxgamers.guiApi.endpoint.surface.display.DisplayInterface]
 */
interface GuiSurfaceInterface {

    /**
     * Registriert ein [ComponentEndpoint] für den Empfang Events
     * @throws [net.bestlinuxgamers.guiApi.endpoint.surface.util.SurfaceAlreadyInUseException] wenn sich bereits registriert wurde
     */
    @SurfaceManagerOnly
    fun registerEndpoint(endpoint: ComponentEndpoint)

    /**
     * Entfernt das momentan registrierte [ComponentEndpoint].
     * Sollte erst nach dem schließen des surfaces aufgerufen werden!
     */
    @SurfaceManagerOnly
    fun unregisterEndpoint()

    /**
     * Öffnet das Surface
     * @param items Items, welche das Surface initial beinhalten soll
     */
    @SurfaceManagerOnly
    fun open(items: Array<ItemStack?>)

    /**
     * @return Ob das Surface geöffnet ist
     */
    @SurfaceManagerOnly
    fun isOpened(): Boolean

    /**
     */
    @SurfaceManagerOnly
    fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?)

    /**
     * Initiiert die Schließung des Surfaces.
     * Hat ein Event zu Folge, welches [onClose] aufruft.
     */
    @SurfaceManagerOnly
    fun close()

    /**
     * Startet alle aktionen, welche nach dem Schließen des Surface ausgeführt werden müssen.
     */
    @EventDispatcherOnly
    fun onClose()

    /**
     * @return die [ReservedSlots] des Surfaces
     */
    @SurfaceManagerOnly
    fun generateReserved(): ReservedSlots
}
