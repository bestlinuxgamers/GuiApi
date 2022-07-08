package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.util.*
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
    val static: Boolean = false, //TODO dynamic (erst bei Änderung in Komponenten rendern)
    val removable: Boolean = false, //TODO
    val renderFallback: ItemStack? = null
) {

    private val components: Array<ComponentIndexMap?> = Array(reservedSlots.totalReserved) { null }
    private var clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit =
        { _: InventoryClickEvent, _: Int -> }

    //render vars
    private var lastRender: Array<ItemStack?>? = null

    internal fun getLastRender() = lastRender?.clone()

    //locks
    private var hook: GuiComponent? = null
    private var locked = false

    //init
    init {
        setUp() //TODO evtl. erst beim start vom rendern aufrufen - muss auch vor erster Änderung (z.B. durch setComponent()) ausgeführt werden
    }

    //hook/lock
    /**
     * Sperrt die Instanz der Komponente für die Nutzung durch eine andere Komponente
     * @param guiComponent Komponente, welche die Sperrung veranlasst
     * @throws ComponentAlreadyInUseException Falls die Komponente bereits verwendet wird
     * @throws ComponentRekursionException Fall eine Rekursion an Komponenten entstehen würde
     * @see isLocked
     */
    private fun hook(guiComponent: GuiComponent) {
        if (isLocked()) throw ComponentAlreadyInUseException()
        if (guiComponent === this) throw ComponentRekursionException()
        val guiComponentHook = guiComponent.hook
        if (guiComponentHook != null) {
            var lastComponent: GuiComponent? = guiComponentHook
            while (lastComponent != null) {
                if (lastComponent === this) throw ComponentRekursionException()
                lastComponent = lastComponent.hook
            }
        }

        hook = guiComponent
    }

    /**
     * Sperrt die Instanz der Komponente für die Nutzung durch eine andere Komponente
     * @throws ComponentAlreadyInUseException Wenn die Komponente bereits gesperrt ist
     * @see isLocked
     */
    internal fun lock() {
        if (isLocked()) throw ComponentAlreadyInUseException()
        locked = true
    }

    /**
     * @return Ob die Komponente gesperrt ist
     * @see hook
     * @see lock
     */
    internal fun isLocked() = (locked || (hook != null))

    /**
     * @return Die übergeordnete Komponente
     * @see hook
     */
    fun getParentComponent() = hook

    //user defined methods

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

    //editing

    /**
     * Setzt eine [GuiComponent] in die Komponentenliste
     * @param component [GuiComponent], welche hinzugefügt werden soll
     * @param start Index in dieser Komponente, an den Index 0 der hinzuzufügenden Komponente gesetzt werden soll
     * @throws ArrayIndexOutOfBoundsException Falls die [component] nicht in den Platz dieser [GuiComponent] passt
     * @throws ComponentOverlapException Falls die [component] eine andere [GuiComponent] überlappen würde
     * @throws ComponentAlreadyInUseException Falls die Instanz der Komponente bereits verwendet wird
     * @throws ComponentRekursionException Fall eine Rekursion an Komponenten entstehen würde
     * @throws SlotNotReservedException Wenn die Komponente auf einen Slot gesetzt werden soll, welcher nicht verfügbar ist
     * @see hook
     */
    fun setComponent(component: GuiComponent, start: Int) {
        component.hook(this) //TODO erst nach am Ende hooken

        val startPosition = reservedSlots.getPosOfReservedIndex(start)
        val componentReservedMapped: ArrayList<Int> = ArrayList()

        component.reservedSlots.getArr2D().forEachIndexed { line, lineData ->
            lineData.forEachIndexed { row, rowData ->
                if (rowData) {
                    componentReservedMapped.add(
                        reservedSlots.getReservedIndexOfPos(
                            Position2D(row + startPosition.x, line + startPosition.y)
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

    /**
     * Entfernt eine Kind-Komponente
     * @param component Komponente
     */
    fun removeComponent(component: GuiComponent) {
        components.forEachIndexed { index, componentMap ->
            if (componentMap?.component == component) {
                components[index] = null
            }
        }
    }

    /**
     * Entfernt alle Komponenten
     */
    fun removeAllComponents() {
        components.forEachIndexed { index, _ -> components[index] = null}
    }

    /**
     * Methode zum Suchen einer Komponente eines Typs
     * @param COMPONENT Typ der gesuchten Komponente
     * @return Komponente des Typs, oder null
     */
    inline fun <reified COMPONENT : GuiComponent> getComponentsOfType(): Set<COMPONENT> {
        val componentClass = COMPONENT::class
        return getComponents().mapNotNull { if (it::class == componentClass) it as COMPONENT else null }.toSet()
    }

    /**
     * @param index Index, dessen Komponente zurückgegeben werden soll
     * @return Komponente an dem Index [index]
     */
    fun getComponentOfIndex(index: Int): GuiComponent? = components[index]?.component

    /**
     * @return alle untergeordneten Komponenten dieser Komponente
     */
    fun getComponents(): Set<GuiComponent> {
        return components.mapNotNull { it?.component }.toSet()
    }

    //rendering

    /**
     * Rendert das nächste Bild unter Berücksichtigung aller Eigenschaften der Komponente
     * @param frame Anzahl des Rendervorgangs
     * @see render
     */
    internal fun renderNextFrame(frame: Long): Array<ItemStack?> {
        if (static) { //TODO statische manuell neu rendern
            lastRender?.let { return it }
        }
        beforeRender(frame)
        return render(frame).also { lastRender = it }
    }

    /**
     * Rendert das nächste Bild
     * @param frame Anzahl des Rendervorgangs
     */
    internal open fun render(frame: Long): Array<ItemStack?> {
        val renderResults: MutableMap<GuiComponent, RenderResultDistributor> = mutableMapOf()
        val output: Array<ItemStack?> = Array(reservedSlots.totalReserved) { renderFallback }

        components.forEachIndexed { index, it ->
            if (it != null) {
                val component: GuiComponent = it.component
                //renderOrCache
                output[index] = (renderResults[component]
                    ?: RenderResultDistributor(component.renderNextFrame(frame)).also { renderResults[component] = it }
                        ).next()
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
    private data class ComponentIndexMap(val component: GuiComponent, val index: Int)

    /**
     * Klasse zum einzelnen Abfragen der Slots eines render Ergebnisses
     * @param result render Ergebnis
     */
    private class RenderResultDistributor(val result: Array<ItemStack?>) {
        private var lastSlot = 0

        /**
         * @return Inhalt des nächsten Slots des [result]
         */
        fun next() = result[lastSlot++]
    }
}
