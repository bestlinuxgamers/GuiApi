package net.bestlinuxgamers.guiApi.templates

import io.mockk.every
import io.mockk.mockk
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemFactory
import java.util.logging.Logger

object MinecraftItemTest {

    const val MOCK_SERVER_VERSION = "UnitTestMc (MC: 1.16.5)"
    var mocked: Boolean = false

    fun mockServer() {
        if (!mocked) {
            val mockServer: Server = mockk(relaxed = true)

            every { mockServer.version } returns MOCK_SERVER_VERSION
            every { mockServer.logger } returns Logger.getAnonymousLogger()
            every { mockServer.itemFactory } returns CraftItemFactory.instance()

            Bukkit.setServer(mockServer)
            mocked = true

            Bukkit.getItemFactory()
                .getItemMeta(Material.ACACIA_BOAT) //Otherwise, enchantments can't be added at the first Test
        }
    }

}
