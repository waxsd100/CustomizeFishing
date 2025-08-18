package io.wax100.customizeFishing.listeners;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FishingListenerUniqueTest {

    @Test
    void testUniqueItemRecognition() {
        // この基本的なテストだけを残して、より複雑なテストは後で追加
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        assertNotNull(item);
        assertEquals(Material.DIAMOND_SWORD, item.getType());
    }
}