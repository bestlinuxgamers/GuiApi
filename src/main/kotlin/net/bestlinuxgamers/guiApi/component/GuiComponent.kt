package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.essentials.ItemComponent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

abstract class GuiComponent(
    private val reservedSlots: ReservedSlots,
    private val static: Boolean = false,
    private val removable: Boolean = false
) {

    init {
        setUp()
    }

    private val components: Array<ComponentIndexMap?> = Array(reservedSlots.size) { null }
    private var clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit =
        { _: InventoryClickEvent, _: Int -> }

    //render_var
    private var lastRender: Array<ItemStack>? = null

    //editing

    abstract fun setUp()

    /**
     * Setzt eine [GuiComponent] in die Komponentenliste
     * @param component [GuiComponent], welches gesetzt werden soll
     * @param start start Index, welcher
     * @throws ArrayIndexOutOfBoundsException Falls die [component] nicht in den Platz dieser [GuiComponent] passt
     * @throws ComponentOverlapException Falls die [component] eine andere [GuiComponent] überlappen würde
     * @throws IllegalArgumentException Falls das [component] in das exakt gleiche [component] gesetzt wird
     */
    fun setComponent(component: GuiComponent, start: Int) {
        if (component == this) throw IllegalArgumentException("Can't add the same component as the parent component")

        val startPosition = reservedSlots.getPosOfIndex(start)
        val componentReservedMapped: ArrayList<Int> = ArrayList()

        component.reservedSlots.getArr2D().forEachIndexed { line, lineData ->
            lineData.forEachIndexed { row, rowData ->
                if (rowData) {
                    componentReservedMapped.add(
                        reservedSlots.getIndexOfPos(
                            ReservedSlots.Position2D(row + startPosition.x, line + startPosition.y)
                        )
                    )
                }
            }
        }
        componentReservedMapped.forEach {
            if (components[it] != null) throw ComponentOverlapException()
        }
        componentReservedMapped.forEachIndexed { index, mappedSlot ->
            components[mappedSlot] = ComponentIndexMap(component, index)
        }
    }

    fun <T : GuiComponent> getComponent(componentClass: Class<T>): T? {
        getComponents().forEach { if (it.javaClass == componentClass) return it as T }
        return null
    }

    fun getComponents(): Set<GuiComponent> {
        val output = mutableSetOf<GuiComponent>()
        components.forEach { it?.let { it2 -> output.add(it2.component) } }
        return output
    }

    //rendering

    internal fun renderNextFrame(frame: Long): Array<ItemStack> {
        return if (static && lastRender != null) {
            lastRender ?: render(frame)
        } else {
            render(frame)
        }
    }

    internal open fun render(frame: Long): Array<ItemStack> {
        val renderResults: MutableMap<GuiComponent, Array<ItemStack>> = mutableMapOf()
        val output: Array<ItemStack> = Array(reservedSlots.size) { RENDER_FALLBACK }

        components.forEachIndexed { index, it ->
            if (it != null) {
                val component: GuiComponent = it.component
                //renderOrCache
                (renderResults[component]?.get(index) ?: component.renderNextFrame(frame)
                    .also { renderResults[component] = it }[index]).also { output[index] = it }
            }
        }
        return output
    }

    //click

    internal open fun click(event: InventoryClickEvent, clickedSlot: Int) {
        clickAction(event, clickedSlot)
        sendClickToChild(event, clickedSlot)
    }

    private fun sendClickToChild(event: InventoryClickEvent, clickedSlot: Int) {
        components[clickedSlot]?.let { it.component.click(event, it.index) }
    }

    fun setClickable(clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit) {
        this.clickAction = clickAction
    }

    //util

    data class ComponentIndexMap(val component: GuiComponent, val index: Int)

    class ComponentOverlapException : RuntimeException()

    companion object {
        val RENDER_FALLBACK = ItemComponent(null!!).renderNextFrame(0)[0] //TODO
    }
}
