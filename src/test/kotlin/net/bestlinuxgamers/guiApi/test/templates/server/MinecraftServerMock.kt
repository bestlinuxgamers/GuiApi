package net.bestlinuxgamers.guiApi.test.templates.server

import io.mockk.every
import io.mockk.mockk
import net.bestlinuxgamers.guiApi.test.templates.MockExtension
import net.bestlinuxgamers.guiApi.test.templates.MockObject
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemFactory
import org.bukkit.craftbukkit.v1_16_R3.inventory.util.CraftInventoryCreator
import org.bukkit.inventory.InventoryHolder
import java.util.logging.Logger

object MinecraftServerMockObj : MockObject<Server>() {

    override fun createMock(): Server = mockk(relaxed = true)

    override fun applyMock(mockObj: Server) {
        Bukkit.setServer(mockObj)
    }
}

class BasicServerExtension : MockExtension<Server>() {
    override fun applyMock(mockClass: Server) {
        every { mockClass.version } returns MOCK_SERVER_VERSION
        every { mockClass.logger } returns Logger.getAnonymousLogger()
    }

    companion object {
        private const val MOCK_SERVER_VERSION = "UnitTestMc (MC: 1.16.5)"
    }
}

class MinecraftItemExtension : MockExtension<Server>() {
    override val dependencies: Set<MockExtension<Server>> = setOf(BasicServerExtension())

    override fun applyMock(mockClass: Server) {
        every { mockClass.itemFactory } returns CraftItemFactory.instance()

        Bukkit.getItemFactory()
            .getItemMeta(Material.ACACIA_BOAT) //Otherwise, enchantments can't be added at the first Test
    }
}

class MinecraftInventoryExtension : MockExtension<Server>() {
    override val dependencies: Set<MockExtension<Server>> = setOf(BasicServerExtension())

    override fun applyMock(mockClass: Server) {
        every { mockClass.createInventory(any(), any<Int>()) }.answers {
            CraftInventoryCreator.INSTANCE.createInventory(
                it.invocation.args[0]?.let { it2 -> it2 as InventoryHolder },
                it.invocation.args[1] as Int
            )
        }
        every { mockClass.createInventory(any(), any<Int>(), any()) }.answers {
            CraftInventoryCreator.INSTANCE.createInventory(
                it.invocation.args[0]?.let { it2 -> it2 as InventoryHolder },
                it.invocation.args[1] as Int,
                it.invocation.args[2] as String
            )
        }
    }
}
