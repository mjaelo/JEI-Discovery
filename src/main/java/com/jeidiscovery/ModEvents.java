package com.jeidiscovery;

import com.jeidiscovery.data.ItemGroup;
import com.jeidiscovery.discovery.DiscoveryManager;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
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

    public static void sendDiscoveryMessage(String groupName) {
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


    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        String toDimension = event.getTo().location().getPath();
        LOGGER.info("Changed dimension to {}", toDimension);
        checkTrigger(ItemGroup.TriggerType.DIMENSION, toDimension);
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return; // Only run on the server side
        }
        ItemStack itemStack = event.getItem().getItem();
        String itemId = itemStack.getItem().getDescriptionId().split("\\.")[2];
        LOGGER.info("Item picked up: {}", itemId);
        checkTrigger(ItemGroup.TriggerType.ITEM, itemId);
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.level().isClientSide()) {
                return; // Only run on the server side
            }
            LivingEntity killedEntity = event.getEntity();
            String mobId = EntityType.getKey(killedEntity.getType()).toString().split(":")[1];
            LOGGER.info("Mob killed: {}", mobId);
            checkTrigger(ItemGroup.TriggerType.MOB, mobId);
        }
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return; // Only run on the server side
        }
        Advancement advancement = event.getAdvancement();
        if (advancement == null) return;
        String advancementId = advancement.getId().toString();
        LOGGER.info("Advancement earned: {}", advancementId);
        checkTrigger(ItemGroup.TriggerType.ADVANCEMENT, advancementId);
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
