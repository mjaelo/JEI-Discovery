package com.jeidiscovery;

import com.jeidiscovery.data.ItemGroup;
import com.jeidiscovery.discovery.DiscoveryManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = JEIDiscovery.MODID, value = Dist.CLIENT)
public class ModEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        String toDimension = event.getTo().location().getPath();
        LOGGER.info("Changed dimension to {}", toDimension);

        // Get groups based on dimension
        DiscoveryManager manager = DiscoveryManager.getInstance();
        List<ItemGroup> dimensionGroups = manager.getItemGroupsByTrigger(ItemGroup.TriggerType.DIMENSION, toDimension);

        // Check for dimension-based discoveries
        if (!dimensionGroups.isEmpty()) {
            for (ItemGroup group : dimensionGroups) {
                manager.discoverItemGroup(group.groupName());
            }
        }
    }
}
