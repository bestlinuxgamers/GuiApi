package net.bestlinuxgamers.guiApi.event

import net.bestlinuxgamers.guiApi.endpoint.surface.GuiSurface
import net.bestlinuxgamers.guiApi.endpoint.surface.MinecraftGuiSurface
import net.bestlinuxgamers.guiApi.provider.EventRegisterProvider
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class MinecraftGuiEventDispatcher(private val eventRegistration: EventRegisterProvider) {

    private val listening: MutableMap<Player, MinecraftGuiSurface> = mutableMapOf()

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

    internal fun registerListening(player: Player, surface: MinecraftGuiSurface) {
        @OptIn(EventDispatcherOnly::class)
        listening[player]?.initiateClose() //TODO system zur verwaltung von bereits geöffneten Uis (evtl. schließen und wieder öffnen)
        listening[player] = surface
    }

    internal fun unregisterListening(surface: GuiSurface) {
        listening.forEach { (key, value) ->
            if (value === surface) unregisterListening(key)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun unregisterListening(player: Player) {
        listening.remove(player)
    }

    //dispatch

    @EventDispatcherOnly
    internal fun dispatchClickEvent(event: InventoryClickEvent) {
        listening[event.whoClicked]?.dispatchClickEvent(event)
    }

    @EventDispatcherOnly
    internal fun dispatchCloseEvent(event: InventoryCloseEvent) {
        listening[event.player]?.dispatchCloseEvent(event)
    }

}
