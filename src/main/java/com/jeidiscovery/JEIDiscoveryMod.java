package com.jeidiscovery;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(JEIDiscoveryMod.MODID)
public class JEIDiscoveryMod {
    public static final String MODID = "jeidiscovery";
    private static final Logger LOGGER = LogManager.getLogger();

    public JEIDiscoveryMod() {
        LOGGER.info("JEIDiscovery mod constructor");

        // Register the ModEvents class to handle all events
        MinecraftForge.EVENT_BUS.register(new ModEvents());

        // Register our main mod class to listen for FML events
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("JEIDiscovery mod client setup");
    }
    

}
