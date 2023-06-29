package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.component.RenderOnly
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import org.bukkit.inventory.ItemStack

/**
 * Diese Komponente stellt einen Endpunkt des Rendervorgangs dar.
 * Keine untergeordneten Komponenten werden gerendert!
 * @param renderItem Item, welches die Komponente bei einem Rendervorgang zurückgeben soll
 * @param reservedSlots Struktur der Komponente
 */
open class RenderEndpointComponent(private val renderItem: ItemStack?, reservedSlots: ReservedSlots) :
    GuiComponent(
        reservedSlots,
        static = true,
        smartRender = false,
        renderFallback = renderItem,
        componentTick = false
    ) {

    override fun setUp() {}

    override fun beforeRender(frame: Long) {}

    override fun onComponentTick(tick: Long, frame: Long) {}

    @RenderOnly
    override fun render(frame: Long): Array<ItemStack?> = Array(reservedSlots.totalReserved) { renderItem }
}
