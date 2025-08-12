package com.jeidiscovery;

import com.jeidiscovery.discovery.DiscoveryManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static IJeiRuntime jeiRuntime;

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("jeidiscovery", "jei_plugin");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        if (!FMLEnvironment.production) {
            LOGGER.info("JEI Plugin: Registering ingredients");
        }
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        JEIPlugin.jeiRuntime = jeiRuntime;
        LOGGER.info("JEI Runtime available. Hiding items from JEI");

        List<ItemStack> itemsToHide = DiscoveryManager.getInstance().getItemsToHide();
        if (!itemsToHide.isEmpty()) {
            JEIPlugin.hideItems(itemsToHide);
        }
    }

    public static void hideItems(List<ItemStack> items) {
        handleItems(items, false);
    }

    public static void showItems(List<ItemStack> items) {
        handleItems(items, true);
    }

    /**
     * Handles showing/hiding items in JEI. Thread-safe - will execute on the main client thread if needed.
     *
     * @param items The list of items to show
     */
    private static void handleItems(List<ItemStack> items, boolean add) {
        if (items == null || items.isEmpty()) {
            return;
        }

        if (jeiRuntime == null) {
            LOGGER.warn("Cannot show items: JEI runtime not available");
            return;
        }

        Minecraft.getInstance().execute(() -> {
            if (jeiRuntime != null) {
                if (add) {
                    jeiRuntime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, items);
                } else {
                    jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, items);
                }
                LOGGER.debug("{} {} items in JEI", add ? "Showing" : "Hiding", items.size());
            }
        });
    }
}