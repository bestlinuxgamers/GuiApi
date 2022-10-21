package net.bestlinuxgamers.guiApi.extensions

import net.bestlinuxgamers.guiApi.templates.server.MinecraftInventoryExtension
import net.bestlinuxgamers.guiApi.templates.server.MinecraftItemExtension
import net.bestlinuxgamers.guiApi.templates.server.MinecraftServerMockObj
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class InventoryExtensionTest {

    @Test
    fun testWriteItems() {
        val emptyInv = Bukkit.createInventory(null, INV_SIZE)
        emptyInv.writeItems(getArr())
        Assertions.assertArrayEquals(getArr(), emptyInv.toList().toTypedArray())
    }

    @Test
    fun testUpdateItems() {
        val testInv = getTestInv()
        val target = getArr().apply {
            this[0] = ItemStack(Material.BEDROCK).apply { itemMeta = itemMeta?.apply { setDisplayName("Hey") } }
            this[3] = ItemStack(Material.STICK)
            this[7] = ItemStack(Material.STONE)
        }
        val falseMat = ItemStack(Material.COBBLESTONE)
        val updateTarget = target.clone().apply { this[8] = falseMat }

        testInv.updateItems(updateTarget, testInv.toList().toTypedArray().apply { this[8] = falseMat })
        Assertions.assertArrayEquals(target, testInv.toList().toTypedArray())
    }

    @Test
    fun testUpdateItemsSelf() {
        val testInv = getTestInv()
        val target = getArr().apply {
            this[0] = ItemStack(Material.BEDROCK).apply { itemMeta = itemMeta?.apply { setDisplayName("Hey") } }
            this[3] = ItemStack(Material.STICK)
            this[7] = ItemStack(Material.STONE)
        }
        testInv.updateItems(target, null)
        Assertions.assertArrayEquals(target, testInv.toList().toTypedArray())
    }

    private fun getTestInv(): Inventory = Bukkit.createInventory(null, INV_SIZE).apply {
        for (i in 0 until INV_SIZE) {
            this.setItem(i, ItemStack(Material.BARRIER).apply {
                itemMeta = itemMeta?.apply { setDisplayName(i.toString()) }
            })
        }
    }

    private fun getArr(): Array<ItemStack?> = Array(INV_SIZE) {
        ItemStack(Material.BARRIER).apply {
            itemMeta = itemMeta?.apply { setDisplayName(it.toString()) }
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun initServer() {
            MinecraftServerMockObj.apply {
                addExtension(MinecraftItemExtension())
                addExtension(MinecraftInventoryExtension())
            }.apply()
        }

        private const val INV_SIZE = 1 * 9
    }

}
