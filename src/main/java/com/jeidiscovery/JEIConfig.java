package com.jeidiscovery.util;

import com.jeidiscovery.JEIDiscovery;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Handles the configuration for which item groups should be hidden.
 */
public class JEIConfig {
    private static JEIConfig instance;

    private JEIConfig() {}

    public static JEIConfig getInstance() {
        if (instance == null) {
            instance = new JEIConfig();
        }
        return instance;
    }

    public List<ItemStack> getHiddenItemStacks() {
        // TODO: Implement the logic to get a list of all items that should be hidden.
        // This method will likely check conditions and return the appropriate items.
        return Collections.emptyList();
    }
}
