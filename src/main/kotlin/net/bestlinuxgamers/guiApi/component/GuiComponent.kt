package net.bestlinuxgamers.guiApi.component

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Repräsentiert eine Komponente eines Minecraft Guis.
 *
 * Die Klasse erstellt zuerst mithilfe der [setUp] Methode die Komponente.
 * Anschließend können die Komponenten gerendert [renderNextFrame] werden.
 * @param reservedSlots Fläche der Komponente
 * @param static Ob die Komponente nur einmal gerendert werden soll.
 * Dies wird empfohlen, wenn die Komponente keine Animationen o.ä. enthält.
 * Beachte: [beforeRender] wird nur einmal aufgerufen!
 * @param removable Ob Items aus dem Inventar entfernbar sein sollen.
 * @param renderFallback Item, welches auf reservierte aber nicht gerenderte Slots gesetzt wird
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class GuiComponent(
    val reservedSlots: ReservedSlots,
    val static: Boolean = false,
    val removable: Boolean = false, //TODO
    val renderFallback: ItemStack? = null
) {

    init {
        setUp() //TODO evtl. erst beim start vom rendern aufrufen
    }

    private val components: Array<ComponentIndexMap?> = Array(reservedSlots.totalReserved) { null }
    private var clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit =
        { _: InventoryClickEvent, _: Int -> }

    //render vars
    private var lastRender: Array<ItemStack?>? = null
    private var hooked: Boolean = false

    internal fun getLastRender() = lastRender?.clone()

    //editing

    /**
     * Sperrt die Instanz der Komponente für die Nutzung durch eine andere Komponente
     */
    internal fun hook() {
        hooked = true
    }

    /**
     * Richtet die Komponente für den ersten Rendervorgang ein.<br/>
     * Nur neue Instanzen von Komponenten erstellen!
     */
    abstract fun setUp()

    /**
     * Wird vor jedem Rendervorgang aufgerufen.
     * @param frame Bild, welches gerendert werden soll
     */
    abstract fun beforeRender(frame: Long)

    /**
     * Setzt eine [GuiComponent] in die Komponentenliste
     * @param component [GuiComponent], welche hinzugefügt werden soll
     * @param start Index in dieser Komponente, an den Index 0 der hinzuzufügenden Komponente gesetzt werden soll
     * @throws ArrayIndexOutOfBoundsException Falls die [component] nicht in den Platz dieser [GuiComponent] passt
     * @throws ComponentOverlapException Falls die [component] eine andere [GuiComponent] überlappen würde
     * @throws ComponentAlreadyInUseException Falls die Instanz der Komponente bereits verwendet wird.
     */
    fun setComponent(component: GuiComponent, start: Int) {
        if (!component.hooked) component.hook() else throw ComponentAlreadyInUseException()

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

    //TODO deleteComponent()

    /**
     * Methode zum Suchen einer Komponente eines Typs
     * @param COMPONENT Typ der gesuchten Komponente
     * @return Komponente des Typs, oder null
     */
    inline fun <reified COMPONENT : GuiComponent> getComponent(): COMPONENT? { //TODO - was, wenn mehrere Komponenten mit dem gleichen Typ
        val componentClass = COMPONENT::class
        getComponents().forEach { if (it::class == componentClass) return it as COMPONENT }
        return null
    }

    /**
     * @return alle untergeordneten Komponenten dieser Komponente
     */
    fun getComponents(): Set<GuiComponent> { //TODO was, wenn component doppelt
        return components.mapNotNull { it?.component }.toSet()
    }

    //rendering

    /**
     * Rendert das nächste Bild unter Berücksichtigung aller Eigenschaften der Komponente
     * @param frame Anzahl des Rendervorgangs
     * @see render
     */
    internal fun renderNextFrame(frame: Long): Array<ItemStack?> {
        if (static) {
            lastRender?.let { return it }
        }
        beforeRender(frame)
        return render(frame)
    }

    /**
     * Rendert das nächste Bild
     * @param frame Anzahl des Rendervorgangs
     */
    internal open fun render(frame: Long): Array<ItemStack?> {
        val renderResults: MutableMap<GuiComponent, Array<ItemStack?>> = mutableMapOf()
        val output: Array<ItemStack?> = Array(reservedSlots.totalReserved) { renderFallback }

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
    //TODO protection (Items herausnehmen interaktiv blockieren) - Siehe Konstruktor

    /**
     * Setzt die Aktion, welche bei einem Klick ausgeführt wird
     * @param clickAction Aktion
     */
    fun setClickable(clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit) {
        this.clickAction = clickAction
    }

    /**
     * Führt die Klick-Aktion aus
     * @param event Klick-Event, welches den Klick hervorgerufen hat
     * @param clickedSlot Slot dieser Komponente, welcher angeklickt wurde.
     * Beachte: Der Index des Inventars geht über die Indexe aller Teilkomponenten.
     * Daher geht der Index einer Komponente über einen Teil des Gesamtindexes (vom Inventar).
     */
    internal open fun click(event: InventoryClickEvent, clickedSlot: Int) {
        clickAction(event, clickedSlot)
        sendClickToChild(event, clickedSlot)
    }

    /**
     * Sendet eine Klick-Aktion an die geklickte Kind-Komponente weiter
     * @param event Klick-Event, welches den Klick hervorgerufen hat
     * @param clickedSlot Slot dieser Komponente, welcher angeklickt wurde.
     * @see click
     */
    private fun sendClickToChild(event: InventoryClickEvent, clickedSlot: Int) {
        components[clickedSlot]?.let { it.component.click(event, it.index) }
    }

    //util

    /**
     * Klasse, welche eine [GuiComponent] mit einem Index verbindet
     */
    data class ComponentIndexMap(val component: GuiComponent, val index: Int)

    /**
     * Dieser Fehler wird geworfen, wenn sich eine Komponente mit einer anderen überlappt
     */
    class ComponentOverlapException : RuntimeException()

    /**
     * Dieser Fehler wird geworfen, wenn eine [GuiComponent] bereits verwendet wird
     * @see hook
     */
    class ComponentAlreadyInUseException : RuntimeException()

}
