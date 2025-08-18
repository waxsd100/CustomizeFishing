package io.wax100.customizeFishing.unique;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UniqueItemManagerTest {

    @Test
    void testBasicUniqueItemFunctionality() {
        // 基本的なアイテム操作のテスト
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        assertNotNull(item);
        assertEquals(Material.NETHERITE_SWORD, item.getType());
        assertEquals(1, item.getAmount());
    }
}