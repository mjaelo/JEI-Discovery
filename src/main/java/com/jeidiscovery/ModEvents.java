package com.jeidiscovery;

import com.jeidiscovery.data.ItemGroup;
import com.jeidiscovery.discovery.DiscoveryManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = JEIDiscovery.MODID, value = Dist.CLIENT)
public class ModEvents {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String lastKnownBiome;


    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        String toDimension = event.getTo().location().getPath();
        LOGGER.info("Changed dimension to {}", toDimension);
        checkTrigger(ItemGroup.TriggerType.DIMENSION, toDimension);
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide()) {
                return; // Only run on the server side
            }
            MobEffectInstance effect = event.getEffectInstance();
            String effectId = effect.getEffect().getDescriptionId().split("\\.")[2];
            LOGGER.info("Effect applied: {}", effectId);
            checkTrigger(ItemGroup.TriggerType.EFFECT, effectId);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (player.level().isClientSide()) {
                return; // Only run on the server side
            }
            String currentBiome = player.level().getBiome(player.blockPosition()).unwrapKey().get().location().getPath();
            if (!currentBiome.equals(lastKnownBiome)) {
                System.out.println(player.getName().getString() + " has entered the " + currentBiome + " biome.");
                checkTrigger(ItemGroup.TriggerType.BIOME, currentBiome);
                lastKnownBiome = currentBiome;
            }
        }
    }

    private static void checkTrigger(ItemGroup.TriggerType triggerType, String triggerValue) {
        // Get matching groups based on trigger
        DiscoveryManager manager = DiscoveryManager.getInstance();
        List<ItemGroup> matchingGtoups = manager.getItemGroupsByTrigger(triggerType, triggerValue);

        // Trigger discovery if any groups match the condition
        if (!matchingGtoups.isEmpty()) {
            for (ItemGroup group : matchingGtoups) {
                manager.discoverItemGroup(group.groupName());
            }
        }
    }

}
