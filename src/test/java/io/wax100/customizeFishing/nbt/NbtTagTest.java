package io.wax100.customizeFishing.nbt;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NbtTagTest {

    @Test
    void testBasicNbtFunctionality() {
        // 基本的なItemStackのテスト
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        assertNotNull(item);
        assertEquals(Material.DIAMOND_SWORD, item.getType());

        // NBTデータの基本概念をテスト
        NamespacedKey testKey = NamespacedKey.fromString("test:key");
        assertNotNull(testKey);
        assertEquals("test", testKey.getNamespace());
        assertEquals("key", testKey.getKey());
    }
}