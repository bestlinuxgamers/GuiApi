package net.bestlinuxgamers.guiApi.event

import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.provider.EventRegisterProvider
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class MinecraftGuiEventDispatcher(private val eventRegistration: EventRegisterProvider) {

    private val listening: MutableMap<Inventory, MinecraftGuiSurface> = mutableMapOf()

    init {
        registerListener()
    }

    /**
     * Registriert den [MinecraftGuiListener] bei Bukkit
     * @see EventRegisterProvider
     */
    private fun registerListener() {
        eventRegistration.registerListener(MinecraftGuiListener(this))
    }

    //register

    /**
     * @throws IllegalArgumentException
     */
    internal fun registerListening(inventory: Inventory, surface: MinecraftGuiSurface) {
        listening[inventory]?.let { throw IllegalArgumentException("Surface already registered this inventory") }
        listening[inventory] = surface
    }

    internal fun unregisterListening(surface: GuiSurface) {
        listening.forEach { (key, value) ->
            if (value === surface) unregisterListening(key)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun unregisterListening(inventory: Inventory) {
        listening.remove(inventory)
    }

    //dispatch

    @EventDispatcherOnly
    internal fun dispatchClickEvent(event: InventoryClickEvent) {
        listening[event.inventory]?.dispatchClickEvent(event)
    }

    @EventDispatcherOnly
    internal fun dispatchCloseEvent(event: InventoryCloseEvent) {
        listening[event.inventory]?.dispatchCloseEvent(event)
    }
}
