package io.wax100.customizeFishing.fishing;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class FishingConditionChecker {
    
    public static boolean isOpenWater(Location hookLocation) {
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    
                    Block block = hookLocation.clone().add(x, y, z).getBlock();
                    Material type = block.getType();
                    
                    if (!type.equals(Material.WATER) && 
                        !type.equals(Material.AIR) && 
                        !type.equals(Material.LILY_PAD) &&
                        !type.equals(Material.SEAGRASS) &&
                        !type.equals(Material.TALL_SEAGRASS) &&
                        !type.equals(Material.KELP) &&
                        !type.equals(Material.KELP_PLANT)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
}