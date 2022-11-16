package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.util.*
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.extensions.getValueSaved
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Repräsentiert eine Komponente eines Minecraft Guis.
 *
 * Die Klasse erstellt zuerst mithilfe der [setUp] Methode die Komponente.
 * Anschließend können die Komponenten gerendert [renderNextFrame] werden.
 * @param reservedSlots Fläche der Komponente
 * @param static Ob die Komponente nur einmal gerendert werden soll.
 * Dies wird empfohlen, wenn die Komponente keine Animationen/Veränderungen enthält.
 * Beachte: [beforeRender] wird nur einmal aufgerufen!
 * @param smartRender Ob nur Komponenten mit detektierten Veränderungen gerendert werden sollen.
 * @param renderFallback Item, welches auf reservierte aber nicht gerenderte Slots gesetzt wird
 */
abstract class GuiComponent(
    val reservedSlots: ReservedSlots,
    val static: Boolean = false,
    val smartRender: Boolean = true,
    val renderFallback: ItemStack? = null
) {

    private val components: Array<ComponentIndexMap?> = Array(reservedSlots.totalReserved) { null }
    private var clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit =
        { _: InventoryClickEvent, _: Int -> }

    //render vars
    private var lastRender: Array<ItemStack?>? = null

    internal fun getLastRender() = lastRender?.clone()

    private val changedSlots: MutableSet<Int> = mutableSetOf()

    //locks
    private var hook: GuiComponent? = null
    private var locked = false

    //init
    init {
        setUp() //TODO evtl. erst beim start vom rendern aufrufen - muss auch vor erster Änderung (z.B. durch setComponent()) ausgeführt werden - kann in ItemGui benutzt werden
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
        if (testForRekursion(guiComponent)) throw ComponentRekursionException()
        hook = guiComponent
    }

    /**
     * Testet, ob mit einer Komponente eine Rekursion entstehen würde
     * @param component Komponente, mit der auf eine Rekursion getestet werden soll
     * @return Ob eine Rekursion entsteht
     */
    private fun testForRekursion(component: GuiComponent): Boolean {
        if (component === this) return true
        val guiComponentHook = component.hook
        if (guiComponentHook != null) {
            var lastComponent: GuiComponent? = guiComponentHook
            while (lastComponent != null) {
                if (lastComponent === this) return true
                lastComponent = lastComponent.hook
            }
        }
        return false
    }

    /**
     * Entfernt den hook der Komponente
     * @see hook
     */
    private fun unHook() {
        hook = null
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
    @Suppress("MemberVisibilityCanBePrivate")
    fun isLocked() = (locked || (hook != null))

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

    //TODO fun addBeforeRender((Int) -> Unit) zum Hinzufügen von Aktionen von außen. Dafür evtl. Liste an lambdas, eine ist immer {setUp()}

    /**
     * Wird vor dem Rendern durch den [net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint.renderTick] aufgerufen.
     * Damit wird es noch vor [beforeRender] aufgerufen.
     * @param tick Anzahl des Ticks
     * @param frame Bild, welches nach dem Tick gerendert werden soll
     */
    abstract fun onRenderTick(tick: Long, frame: Long)

    //- call dispatchers

    /**
     * Ruft [beforeRender] für diese und alle untergeordneten Komponenten auf.
     */
    @CallDispatcherOnly
    internal fun dispatchBeforeRender(frame: Long) {
        beforeRender(frame)
        getComponents().forEach { it.dispatchBeforeRender(frame) }
    }

    /**
     * Ruft [onRenderTick] für diese und alle untergeordneten Komponenten auf.
     */
    @CallDispatcherOnly
    internal fun dispatchOnRenderTick(tick: Long, frame: Long) {
        onRenderTick(tick, frame)
        getComponents().forEach { it.dispatchOnRenderTick(tick, frame) }
    }

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
        if (component.isLocked()) throw ComponentAlreadyInUseException()

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
            if (components[it] != null) throw ComponentOverlapException() //TODO genaue Fehlermeldung
        }

        component.hook(this)
        componentReservedMapped.forEachIndexed { index, mappedSlot ->
            components[mappedSlot] = ComponentIndexMap(component, index)
            slotChanged(mappedSlot)
        }
    }

    /**
     * Entfernt eine Kind-Komponente
     * @param component Komponente
     */
    fun removeComponent(component: GuiComponent) {
        if (!getComponents().contains(component)) return

        component.unHook()
        components.forEachIndexed { index, componentMap ->
            if (componentMap?.component == component) {
                components[index] = null
                slotChanged(index)
            }
        }
    }

    /**
     * Entfernt alle Komponenten
     */
    fun removeAllComponents() {
        getComponents().forEach { removeComponent(it) }
    }

    /**
     * Speichert, dass sich ein Slot verändert hat und beim nächsten smartRender Vorgang erneut gerendert werden soll.
     * Diese Information wird auch an alle übergeordneten Komponenten weitergegeben.
     * @see smartRender
     */
    private fun slotChanged(slot: Int) {
        changedSlots.add(slot)
        val parent = getParentComponent() ?: return
        parent.getLocalIndexOfComponentIndex(this, slot).forEach { parent.slotChanged(it) }
    }

    //component getters

    /**
     * Methode zum Suchen einer Komponente eines Typs
     * @param COMPONENT Typ der gesuchten Komponente
     * @return Komponente des Typs, oder null
     */
    inline fun <reified COMPONENT : GuiComponent> getComponentsOfType(): Set<COMPONENT> {
        return getComponents().mapNotNull { if (it is COMPONENT) it else null }.toSet()
    }

    /**
     * @param index Index, dessen Komponente zurückgegeben werden soll
     * @return Komponente an dem Index [index]
     */
    fun getComponentOfIndex(index: Int): GuiComponent? = components[index]?.component

    /**
     * Gibt die Indexe einer Unterkomponente mit den jeweiligen Indexen dieser Komponente,
     * auf denen der Unterkomponenten-Index liegt, zurück.
     * @param component Komponente, nach dessen Referenzen in dieser Komponente gesucht werden soll
     * @return Map aus dem Index der [component] zu einem Set aus Indexen,
     * auf denen jener [component]-Index auf dieser Komponente liegt.
     */
    fun getComponentIndexToLocalIndexMap(component: GuiComponent): Map<Int, Set<Int>> {
        val output = mutableMapOf<Int, MutableSet<Int>>().withDefault { mutableSetOf() }
        components.forEachIndexed { index, cim ->
            if (cim != null && cim.component == component) {
                output.getValueSaved(cim.index).add(index)
            }
        }
        return output
    }

    /**
     * Gibt die Indexe dieser Komponente zu den Indexen einer gesuchten Komponente,
     * welche auf dem Index dieser Komponente liegen, zurück.
     * @param component Komponente, nach dessen Referenzen in dieser Komponente gesucht werden soll
     * @return Map aus dem Index dieser Komponente zu dem Index der [component],
     * welcher auf diesem Index liegt.
     */
    fun getLocalIndexToComponentIndexMap(component: GuiComponent): Map<Int, Int> {
        val output = mutableMapOf<Int, Int>()
        components.forEachIndexed { index, cim ->
            if (cim != null && cim.component == component) {
                output[index] = cim.index
            }
        }
        return output
    }

    /**
     * Gibt den Index von dieser Komponente zurück, auf dem der Index einer Unterkomponente liegt.
     * @param component Unterkomponente, nach dessen [index] gesucht wird
     * @param index Index der [component]
     * @return Index (Slot) in dieser Komponente, auf dem der angegebene Unterkomponenten-Index liegt.
     * Es wird ein Set zurückgegeben, da es möglich ist, dass ein Unterkomponenten-Index
     * auf mehreren Slots dieser Komponente liegt.
     */
    fun getLocalIndexOfComponentIndex(component: GuiComponent, index: Int): Set<Int> {
        return components.mapIndexed { idx, it ->
            it?.let { it2 ->
                if (it2.component == component && it2.index == index) return@mapIndexed idx
            }
            null
        }.filterNotNull().toSet()
    }

    /**
     * @return alle untergeordneten Komponenten dieser Komponente
     */
    fun getComponents(): Set<GuiComponent> {
        return components.mapNotNull { it?.component }.toSet()
    }

    //user-render-interaction

    /**
     * Reicht den Befehl, einen Rendervorgang zu starten, an die rendernde Komponente weiter.
     * Sollte diese Funktionalität in der rendernden Komponente deaktiviert worden sein,
     * wird der Rendervorgang nicht ausgeführt.
     * @see net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint.onDemandRender
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun triggerReRender() {
        @OptIn(CallDispatcherOnly::class)
        passUpTriggerReRender()
    }

    /**
     * Reicht den [triggerReRender] call an die höchstgelegene Komponente weiter,
     * welche diese Methode überschreibt.
     */
    @CallDispatcherOnly
    internal open fun passUpTriggerReRender(){
        getParentComponent()?.triggerReRender()
    }

    //rendering

    /**
     * Rendert das nächste Bild unter Berücksichtigung aller Eigenschaften der Komponente
     * @param frame Anzahl des Rendervorgangs
     * @see render
     * @see smartRender
     */
    @RenderOnly
    internal fun renderNextFrame(frame: Long): Array<ItemStack?> {
        if (static) { //TODO statische manuell neu rendern
            lastRender?.let { return it }
        }
        //TODO evtl. onRender() / afterRender() - nur für zuvor gerenderte
        return (if (smartRender) smartRender(frame) else render(frame)).also { lastRender = it }
    }

    /**
     * Rendert das nächste Bild.
     * @param frame Anzahl des Rendervorgangs
     */
    @RenderOnly
    internal open fun render(frame: Long): Array<ItemStack?> {
        changedSlots.clear()
        val renderManager = RenderManager(frame)
        return Array(reservedSlots.totalReserved) {
            val compIndex = components[it] ?: return@Array renderFallback
            return@Array renderManager.getComponentIndex(compIndex.component, compIndex.index) ?: renderFallback
        }
    }

    /**
     * Rendert das nächste Bild.
     * Dabei werden nur Komponenten gerendert, welche auf Slots liegen, die verändert wurden.
     * Bei nicht veränderten Komponenten werden die Ergebnisse des letzten render-Vorgangs benutzt.
     * @param frame Anzahl des Rendervorgangs
     * @see slotChanged
     * @see render
     */
    @RenderOnly
    internal open fun smartRender(frame: Long): Array<ItemStack?> {
        val output = getLastRender() ?: return render(frame)
        val renderManager = RenderManager(frame)

        changedSlots.forEach {
            output[it] = components[it]?.let { it2 -> renderManager.getComponentIndex(it2.component, it2.index) }
                ?: renderFallback
        }
        changedSlots.clear()
        return output
    }

    //click
    //TODO protection (Items herausnehmen interaktiv blockieren)

    /**
     * Setzt die Aktion, welche bei einem Klick ausgeführt wird
     * @param clickAction Aktion
     */
    @Suppress("unused")
    fun setClickable(clickAction: (event: InventoryClickEvent, clickedComponent: Int) -> Unit) { //TODO nicht bei static
        this.clickAction = clickAction
    }

    /**
     * Führt die Klick-Aktion aus
     * @param event Klick-Event, welches den Klick hervorgerufen hat
     * @param clickedSlot Slot dieser Komponente, welcher angeklickt wurde.
     * Beachte: Der Index des Inventars geht über die Indexe aller Teilkomponenten.
     * Daher geht der Index einer Komponente über einen Teil des Gesamtindexes (vom Inventar).
     */
    @EventDispatcherOnly
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
    @EventDispatcherOnly
    private fun sendClickToChild(event: InventoryClickEvent, clickedSlot: Int) {
        components[clickedSlot]?.let { it.component.click(event, it.index) }
    }

    //util

    /**
     * Klasse, welche eine [GuiComponent] mit einem Index verbindet
     */
    private data class ComponentIndexMap(val component: GuiComponent, val index: Int)

    /**
     * Klasse zum Zwischenspeichern von den render-Ergebnissen der Unterkomponenten.
     * @param frame Anzahl des Rendervorgangs
     */
    private class RenderManager(private val frame: Long) {

        private val renderResults: MutableMap<GuiComponent, Array<ItemStack?>> = mutableMapOf()

        /**
         * Gibt einen gewissen Slot eines render-Ergebnisses zurück.
         * Wenn noch kein render-Ergebnis der angegebenen Komponente vorliegt, wird diese gerendert.
         * @param component Komponente, aus dessen render-Ergebnis ein Slot abgefragt wird
         * @param index Slot, welcher aus dem render-Ergebnis benötigt wird
         * @return gerendertes Item auf dem Slot der Komponente
         */
        @RenderOnly
        fun getComponentIndex(component: GuiComponent, index: Int): ItemStack? {
            return (renderResults[component]
                ?: component.renderNextFrame(frame)
                    .also { renderResults[component] = it }
                    )[index]
        }

        @RenderOnly
        fun getRenderedComponents() = renderResults.keys

    }
}
