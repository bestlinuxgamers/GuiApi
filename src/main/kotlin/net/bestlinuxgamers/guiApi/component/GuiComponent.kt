package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.essentials.ItemComponent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Repräsentiert eine Komponente eines Minecraft Guis.<br/>
 * Die Klasse erstellt zuerst mithilfe der [setUp] Methode die Komponente.
 * Anschließend können die Komponenten gerendert [renderNextFrame] werden.
 * @param reservedSlots Fläche der Komponente
 * @param static Ob die Komponente nur einmal gerendert werden soll.
 * Dies wird empfohlen, wenn die Komponente keine Animationen o.ä. enthält.
 * Beachte: [beforeRender] wird nur einmal aufgerufen!
 * @param removable Ob Items aus dem Inventar entfernbar sein sollen.
 */
abstract class GuiComponent(
    private val reservedSlots: ReservedSlots,
    private val static: Boolean = false,
    private val removable: Boolean = false //TODO
) {

    init {
        setUp() //TODO evtl. erst beim start vom rendern aufrufen
    }

    private val components: Array<ComponentIndexMap?> = Array(reservedSlots.size) { null }
    private var clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit =
        { _: InventoryClickEvent, _: Int -> }

    //render vars
    private var lastRender: Array<ItemStack>? = null
    //TODO render hook, damit Instanz nicht doppelt verwendet wird

    //editing

    /**
     * Richtet die Komponente für den ersten Rendervorgang ein.<br/>
     * Nur neue Instanzen von Komponenten erstellen!
     */
    abstract fun setUp()

    //TODO abstract fun beforeRender(frame: Long)

    /**
     * Setzt eine [GuiComponent] in die Komponentenliste
     * @param component [GuiComponent], welche hinzugefügt werden soll
     * @param start Index in dieser Komponente, an den Index 0 der hinzuzufügenden Komponente gesetzt werden soll
     * @throws ArrayIndexOutOfBoundsException Falls die [component] nicht in den Platz dieser [GuiComponent] passt
     * @throws ComponentOverlapException Falls die [component] eine andere [GuiComponent] überlappen würde
     * @throws ComponentRecursionException Falls eine Rekursion mit der [component] entstehen würde
     */
    fun setComponent(component: GuiComponent, start: Int) {
        if (component === this) throw ComponentRecursionException("Can't add the same component as the parent component") //TODO rekursion bei 2+ gleich großen Komponenten verhindern

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
    inline fun <reified COMPONENT : GuiComponent> getComponent(): COMPONENT? { //TODO was, wenn mehrere Komponenten mit dem gleichen Typ
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
    internal fun renderNextFrame(frame: Long): Array<ItemStack> {
        return if (static && lastRender != null) {
            lastRender ?: render(frame)
        } else {
            render(frame)
        }
    }

    /**
     * Rendert das nächste Bild
     * @param frame Anzahl des Rendervorgangs
     */
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
     * Dieser Fehler wird geworfen, wenn sich die Instanz einer Komponente als Kind in derselben Instanz
     * oder einer Kindkomponente dieser befindet und so eine Rekursion entsteht.
     */
    class ComponentRecursionException : RuntimeException {
        @Suppress("UNUSED")
        constructor() : super()
        constructor(message: String) : super(message)
    }

    companion object {
        val RENDER_FALLBACK = ItemComponent(null!!).renderNextFrame(0)[0] //TODO
    }
}
