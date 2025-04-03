package net.bestlinuxgamers.guiApi.test.consoleGui

import io.mockk.mockk
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots
import net.bestlinuxgamers.guiApi.endpoint.ComponentEndpoint
import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurfaceInterface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.test.SchedulerRunnableExtractor
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.security.MessageDigest

/**
 * GUI-Oberfläche, welche das GUI in der Java-Konsole anzeigt.
 * Dabei werden Items mithilfe von zwei Buchstaben dargestellt.
 * Der erste Buchstabe ist der erste Buchstabe des Material-Namens.
 * Der zweite Buchstabe ist der erste Buchstabe das MD5-Hashes des Material-Namens.
 * Dies ist nur für Testzwecke geeignet.
 * Um zu funktionieren, wird die mockk Bibliothek benötigt.
 *
 * Es werden folgende Eingaben unterstützt:
 * - (Zahl) - Zählt als klick auf den Slot mit dem index der Eingabe.
 * - next - Geht einen Tick nach vorne.
 * - next (Zahl) - Geht die angegebene Anzahl von Ticks nach vorne.
 * - close - Schließt das Inventar.
 * @param height Höhe der Anzeige-Fläche.
 * @param width Breite der Anzeige-Fläche.
 * @param scheduler Möglichkeit, um die Ticks zu steuern.
 * @param debug Ob debug-ausgaben aktiviert werden sollen.
 */
class ConsoleGuiSurface(
    private val height: Int,
    private val width: Int,
    private val scheduler: SchedulerRunnableExtractor?,
    private val debug: Boolean = false
) : GuiSurfaceInterface {
    private var endpoint: ComponentEndpoint? = null
    private var running = false

    private fun debug(message: String) {
        if (debug) println(message)
    }

    private fun printItems(items: Array<ItemStack?>) {
        for (row in 0 until height) {
            val rowString = StringBuilder()
            for (col in 0 until width) {
                rowString.append(DISPLAY_SPACER)
                val index = row * width + col
                val item = items.getOrNull(index)

                if (item == null) {
                    rowString.append(SPACE)
                } else {
                    val materialName = item.type.name
                    val materialChar = materialName.first().uppercaseChar()
                    val md5Char = MessageDigest.getInstance("MD5")
                        .digest(materialName.toByteArray())
                        .joinToString("") { "%02x".format(it) }
                        .first()
                    rowString.append(materialChar).append(md5Char)
                }
            }
            rowString.append(DISPLAY_SPACER)
            println(rowString.toString())
        }
    }


    @SurfaceManagerOnly
    override fun registerEndpoint(endpoint: ComponentEndpoint) {
        debug("Endpoint registered.")
        this.endpoint = endpoint
    }

    @SurfaceManagerOnly
    override fun unregisterEndpoint() {
        debug("Endpoint unregistered.")
        endpoint = null
    }

    @SurfaceManagerOnly
    override fun open(items: Array<ItemStack?>) {
        debug("Surface opened.")
        running = true
        printItems(items)
        while (running) {
            val read = readLine() ?: break
            handleInput(read)
        }
    }

    @OptIn(EventDispatcherOnly::class, SurfaceManagerOnly::class)
    private fun handleInput(input: String) {
        val inputStr = input.lowercase()
        if (inputStr == "close") {
            close()
        } else if (inputStr.startsWith("next")) {
            val steps: Int = with(inputStr.split(" ")) {
                if (size == 2) {
                    return@with this[1].toIntOrNull() ?: 1
                }
                return@with 1
            }
            for (i in 0 until steps) {
                scheduler?.get()?.run()
            }
        } else {
            val clickSlot = input.toIntOrNull()
            if (clickSlot == null) {
                println("Not a valid input!")
                return
            }
            val event: InventoryClickEvent = mockk(relaxed = true)
            endpoint?.click(event, clickSlot)
        }
    }

    @SurfaceManagerOnly
    override fun isOpened(): Boolean {
        return running
    }

    @SurfaceManagerOnly
    override fun updateItems(items: Array<ItemStack?>, lastItems: Array<ItemStack?>?) {
        debug("Update Items.")
        printItems(items)
    }

    @OptIn(EventDispatcherOnly::class)
    @SurfaceManagerOnly
    override fun close() {
        debug("Close-request.")
        endpoint?.performClose()
    }

    @EventDispatcherOnly
    override fun onClose() {
        running = false
        println("--- GUI CLOSED ---")
    }

    @SurfaceManagerOnly
    override fun generateReserved(): ReservedSlots {
        return ReservedSlots(height, width)
    }

    companion object {
        private const val DISPLAY_SPACER = "|"
        private const val SPACE = "  "
    }
}
