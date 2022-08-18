package net.bestlinuxgamers.guiApi.endpoint

import net.bestlinuxgamers.guiApi.component.GuiComponent
import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.event.EventDispatcherOnly
import net.bestlinuxgamers.guiApi.provider.SchedulerProvider
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

@OptIn(SurfaceManagerOnly::class)
abstract class ComponentEndpoint(
    private val surface: GuiSurface,
    private val schedulerProvider: SchedulerProvider,
    private val tickSpeed: Long = 20,
    static: Boolean = false,
    background: ItemStack? = null
) : GuiComponent(surface.generateReserved(), static, background) {

    private var frameCount: Long = 0
    private var scheduler: BukkitTask? = null

    init {
        super.lock()
        surface.registerEndpoint(this) //TODO evtl. erst in open
    }

    fun open() {
        if (surface.isOpened()) return

        surface.open(renderNext())
        startUpdateScheduler()
    }

    fun close() = surface.close()

    //Event

    @EventDispatcherOnly
    internal fun performClose() {
        stopUpdateScheduler()
        surface.initiateClose()
    }

    @EventDispatcherOnly
    internal fun performClick(clickedSlot: Int, event: InventoryClickEvent) {
        if (clickedSlot < 0 || clickedSlot >= reservedSlots.totalReserved) return
        click(event, clickedSlot)
    }

    //TODO clickRequest

    //render

    private fun renderNext() = renderNextFrame(frameCount++)

    //scheduler

    private fun startUpdateScheduler() {
        if ((scheduler != null) || super.static) return

        scheduler = schedulerProvider.runTaskTimerAsynchronously(tickSpeed, tickSpeed) { performUpdateTick() }
    }

    private fun stopUpdateScheduler() {
        if (!static) {
            scheduler?.cancel().also { scheduler = null }
        }
    }

    private fun performUpdateTick() {
        val lastRender = super.getLastRender()
        surface.updateItems(renderNext(), lastRender)
        //TODO was, wenn render länger, als tickSpeed benötigt //TODO Items sync updaten
    }

}
