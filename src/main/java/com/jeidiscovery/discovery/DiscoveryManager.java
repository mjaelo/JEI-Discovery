package com.jeidiscovery.discovery;

import com.jeidiscovery.JEIPlugin;
import com.jeidiscovery.ModEvents;
import com.jeidiscovery.data.ItemGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class DiscoveryManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static DiscoveryManager INSTANCE;
    private final DiscoveryConfig config;
    private final Map<String, List<ItemStack>> itemsByGroup = new HashMap<>();

    private DiscoveryManager() {
        config = DiscoveryConfig.getInstance();
        indexItemsByGroup();
    }

    public static synchronized DiscoveryManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DiscoveryManager();
        }
        return INSTANCE;
    }

    public List<ItemStack> getItemsForGroup(String groupName) {
        return itemsByGroup.getOrDefault(groupName, Collections.emptyList());
    }

    private void indexItemsByGroup() {
        itemsByGroup.clear();

        // Get all registered items
        Collection<Item> allItems = ForgeRegistries.ITEMS.getValues();

        // For each item, check which groups it belongs to
        for (Item item : allItems) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId == null) continue;

            ItemStack stack = new ItemStack(item);

            // Check each group to see if this item matches
            for (ItemGroup group : config.getItemGroups()) {
                if (isItemFromGroup(group, itemId)) {
                    itemsByGroup.computeIfAbsent(group.groupName(), k -> new ArrayList<>()).add(stack);
                }
            }
        }

        LOGGER.info("Indexed items for {} groups", itemsByGroup.size());
    }

    public boolean isItemFromGroup(ItemGroup group, ResourceLocation itemId) {
        // Check if item's namespace matches any in the group
        if (group.namespaces().stream().anyMatch(ns -> itemId.getNamespace().equals(ns))) {
            return true;
        }

        // Check if item's path contains any of the keywords
        String path = itemId.getPath().toLowerCase();
        return group.keywords().stream().anyMatch(path::contains);
    }

    public boolean isItemGroupDiscovered(String groupName) {
        return config.getDiscoveredGroupNames().contains(groupName);
    }

    public List<ItemGroup> getItemGroupsByTrigger(ItemGroup.TriggerType type, String value) {
        return config.getItemGroups().stream()
                .filter(group -> group.triggerType() == type && group.triggerValue().equals(value))
                .collect(Collectors.toList());
    }

    public void discoverItemGroup(String groupName) {
        if (config.getDiscoveredGroupNames().add(groupName)) {
            LOGGER.info("Discovered new item group: {}", groupName);
            config.saveConfig();
            JEIPlugin.showItems(getItemsForGroup(groupName));
            ModEvents.sendDiscoveryMessage(groupName);
        }
    }

    public List<ItemStack> getItemsToHide() {
        DiscoveryConfig config = DiscoveryConfig.getInstance();
        DiscoveryManager manager = DiscoveryManager.getInstance();

        List<ItemStack> itemsToHide = new ArrayList<>();
        for (ItemGroup group : config.getItemGroups()) {
            if (!manager.isItemGroupDiscovered(group.groupName())) {
                itemsToHide.addAll(manager.getItemsForGroup(group.groupName()));
            }
        }
        return itemsToHide;
    }
}
