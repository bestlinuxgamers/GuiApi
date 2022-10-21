package net.bestlinuxgamers.guiApi.endpoint.surface.util

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

internal class InventoryCacheTest {

    @Test
    fun testSaveCache() {
        val testInv = getTestInv()
        val cache = InventoryCache(testInv, false).apply { saveCache() }
        Assertions.assertArrayEquals(getArr(), cache.getCache())
    }

    @Test
    fun testAutoCache() {
        val testInv = getTestInv()
        val cache = InventoryCache(testInv, true)
        Assertions.assertArrayEquals(getArr(), cache.getCache())
    }

    @Test
    fun testRestoreCache() {
        val testInv = getTestInv()
        val cache = InventoryCache(testInv, true)

        testInv.setItem(4, ItemStack(Material.BARRIER))
        Assertions.assertEquals(ItemStack(Material.BARRIER), testInv.getItem(4))

        cache.restoreCache()
        Assertions.assertArrayEquals(getArr(), cache.getCache())
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
                addExtension(MinecraftInventoryExtension())
                addExtension(MinecraftItemExtension())
            }.apply()
        }

        private const val INV_SIZE = 1 * 9

    }
}
