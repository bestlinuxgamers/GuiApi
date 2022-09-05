package net.bestlinuxgamers.guiApi.event

import net.bestlinuxgamers.guiApi.endpoint.surface.SurfaceManagerOnly
import net.bestlinuxgamers.guiApi.extensions.getValueSaved
import net.bestlinuxgamers.guiApi.provider.EventRegisterProvider
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

/**
 * Klasse zum verarbeiten und verteilen von GUI-spezifischen Events.
 * @param eventRegistration Klasse zum registrieren des Minecraft Event listeners
 * @see MinecraftGuiListener
 */
class MinecraftGuiEventDispatcher(private val eventRegistration: EventRegisterProvider) : SurfaceEventDispatcher {

    private val listening: MutableMap<Player, MutableSet<IdentifiableSurface>> =
        mutableMapOf<Player, MutableSet<IdentifiableSurface>>().withDefault { mutableSetOf() }

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
     * Registriert einen [dispatcher] für den Empfang von zugehörigen Events.
     * @param player Der Spieler, welcher die Events auslösen soll
     * @param identifier Identifier zum zuordnen eines Events zu dem [dispatcher]
     * @param dispatcher Der [SurfaceEventDispatcher], an den ein passendes Event gesendet werden soll.
     * Auf keinen Fall eine Instanz dieser Klasse angeben!
     */
    @SurfaceManagerOnly
    fun registerListening(player: Player, identifier: EventIdentifier, dispatcher: SurfaceEventDispatcher) {
        listening.getValueSaved(player).add(IdentifiableSurface(identifier, dispatcher))
    }

    /**
     * Beendet den Empfang von zugehörigen Events für einen [dispatcher]
     * @param player Spieler, welchem der [dispatcher] zugehörig ist
     * @param dispatcher Dispatcher, welcher entfernt werden soll
     */
    @SurfaceManagerOnly
    fun unregisterListening(player: Player, dispatcher: SurfaceEventDispatcher) {
        listening[player]?.removeIf { it.dispatcher == dispatcher }
    }

    /**
     * Beendet den Empfang von zugehörigen Events für alle [SurfaceEventDispatcher] eines [player]s
     * @param player Spieler, dessen [SurfaceEventDispatcher] keine Events mehr bekommen sollen
     */
    @SurfaceManagerOnly
    @Suppress("unused")
    fun unregisterListening(player: Player) {
        listening.remove(player)
    }

    //dispatch

    /**
     * Leitet das Event an alle passenden dispatcher weiter.
     * Die zugehörigkeit wird mithilfe des bereits registrierten [EventIdentifier] bestimmt.
     * @see registerListening
     */
    @EventDispatcherOnly
    override fun dispatchClickEvent(event: InventoryClickEvent) {
        listening[event.whoClicked]?.forEach {
            if (it.identifier.isClickEvent(event)) it.dispatcher.dispatchClickEvent(event)
        }
    }

    /**
     * Leitet das Event an alle passenden dispatcher weiter.
     * Die zugehörigkeit wird mithilfe des bereits registrierten [EventIdentifier] bestimmt.
     * @see registerListening
     */
    @EventDispatcherOnly
    override fun dispatchCloseEvent(event: InventoryCloseEvent) {
        listening[event.player]?.forEach {
            if (it.identifier.isCloseEvent(event)) it.dispatcher.dispatchCloseEvent(event)
        }
    }

    //util

    /**
     * Klasse, welche einen [SurfaceEventDispatcher] mit einem [EventIdentifier] verbindet.
     */
    private class IdentifiableSurface(val identifier: EventIdentifier, val dispatcher: SurfaceEventDispatcher)

}
