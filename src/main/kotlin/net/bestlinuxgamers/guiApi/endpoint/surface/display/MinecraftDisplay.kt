package net.bestlinuxgamers.guiApi.endpoint.surface.display

import org.bukkit.entity.Player

/**
 * Eine grafische Oberfläche in Minecraft
 */
interface MinecraftDisplay : DisplayInterface {

    val player: Player

    companion object {
        const val INVENTORY_WIDTH = 9
    }
}
