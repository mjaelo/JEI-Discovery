package com.jeidiscovery.discovery;

import com.jeidiscovery.JEIPlugin;
import com.jeidiscovery.data.ItemGroup;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        IJeiRuntime jeiRuntime = JEIPlugin.getJeiRuntime();
        if (jeiRuntime == null) return;

        IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
        List<ItemStack> allItemStacks = ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK).stream().toList();

        for (ItemStack stack : allItemStacks) {
            if (stack.isEmpty()) continue;
            String itemId = stack.getDescriptionId();
            for (ItemGroup group : config.getItemGroups()) {
                if (isItemFromGroup(group, itemId)) {
                    itemsByGroup.computeIfAbsent(group.groupName(), k -> new ArrayList<>()).add(stack.copy());
                }
            }
        }
        LOGGER.info("Indexed items for {} groups", itemsByGroup.size());
    }

    public boolean isItemFromGroup(ItemGroup group, String itemId) {
        if (!group.namespaces().isEmpty() && group.namespaces().stream().noneMatch(itemId::contains)) return false;
        return  group.blacklist().stream().noneMatch(itemId::contains) && group.keywords().stream().anyMatch(itemId::contains);
    }

    public boolean isItemGroupDiscovered(String groupName) {
        return config.getDiscoveredGroupNames().contains(groupName);
    }

    public List<ItemGroup> getItemGroupsByTrigger(ItemGroup.TriggerType type, String value) {
        return config.getItemGroups().stream()
                .filter(group -> group.triggerType() == type && value.contains(group.triggerValue()))
                .collect(Collectors.toList());
    }

    public void discoverItemGroup(String groupName) {
        if (config.getDiscoveredGroupNames().add(groupName)) {
            LOGGER.info("Discovered new item group: {}", groupName);
            config.saveConfig();
            List<ItemStack> itemsForGroup = getItemsForGroup(groupName).stream()
                    .filter(discovered->
                            getItemsToHide().stream()
                                    .noneMatch(stack-> stack.getItem().getDescriptionId().equals(discovered.getItem().getDescriptionId()))
                    ).toList();
            JEIPlugin.showItems(itemsForGroup);
            sendDiscoveryMessage(groupName);
        }
    }

    public List<ItemStack> getItemsToHide() {
        if (itemsByGroup.isEmpty()) {
            indexItemsByGroup();
        }
        DiscoveryConfig config = DiscoveryConfig.getInstance();
        List<ItemStack> itemsToHide = new ArrayList<>();
        for (ItemGroup group : config.getItemGroups()) {
            if (!isItemGroupDiscovered(group.groupName())) {
                itemsToHide.addAll(getItemsForGroup(group.groupName()));
            }
        }
        return itemsToHide;
    }

    private void sendDiscoveryMessage(String groupName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            MutableComponent message = Component.literal("Discovered new item group: ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
                    .append(Component.literal(groupName).withStyle(
                            Style.EMPTY
                                    .withColor(ChatFormatting.GOLD)
                                    .withBold(true)
                    ));
            minecraft.player.displayClientMessage(message, false);
        }
    }
}
