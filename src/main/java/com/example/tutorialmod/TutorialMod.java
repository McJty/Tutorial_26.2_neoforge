package com.example.tutorialmod;

import com.example.tutorialmod.datagen.ModDataGenerators;
import com.example.tutorialmod.integration.top.TopIntegration;
import com.example.tutorialmod.registration.ModBlockEntities;
import com.example.tutorialmod.registration.ModBlocks;
import com.example.tutorialmod.registration.ModDataComponents;
import com.example.tutorialmod.registration.ModItems;
import com.example.tutorialmod.registration.ModMenus;
import com.example.tutorialmod.registration.ModCreativeTabs;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

/**
 * The main class for the mod.
 *
 * <p>NeoForge creates this class when it finds the {@link Mod} annotation.</p>
 */
@Mod(TutorialMod.MOD_ID)
public final class TutorialMod {
    // This must match mod_id in gradle.properties.
    public static final String MOD_ID = "tutorialmod";

    private static final Logger LOGGER = LogUtils.getLogger();

    public TutorialMod(IEventBus modEventBus) {
        ModDataComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        modEventBus.addListener(ModBlockEntities::registerCapabilities);

        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(GatherDataEvent.Client.class, ModDataGenerators::gatherClientData);
        modEventBus.addListener(GatherDataEvent.Server.class, ModDataGenerators::gatherServerData);
        modEventBus.addListener(TutorialMod::enqueueInterModCommunication);

        LOGGER.info("Tutorial Mod is loading!");
    }

    private static void enqueueInterModCommunication(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            TopIntegration.register();
        }
    }
}
