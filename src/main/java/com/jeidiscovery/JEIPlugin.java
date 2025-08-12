package com.jeidiscovery.jei;

import com.jeidiscovery.JEIDiscovery;
import com.jeidiscovery.util.JEIConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static IJeiRuntime jeiRuntime;
    public static final ResourceLocation PLUGIN_UID = new ResourceLocation(JEIDiscovery.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // TODO: Handle static recipe registration if any.
        updateHiddenItems();
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime runtime) {
        jeiRuntime = runtime;
        updateHiddenItems();
    }

    public static void updateHiddenItems() {
        if (jeiRuntime == null) {
            return;
        }

        try {
            var hiddenItems = JEIConfig.getInstance().getHiddenItemStacks();
            if (!hiddenItems.isEmpty()) {
                // JEI's runtime API to remove ingredients at runtime.
                jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hiddenItems);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update JEI hidden items", e);
        }
    }
}
