package com.example.tutorialmod.client;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.registration.ModBlockEntities;
import com.example.tutorialmod.registration.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TutorialMod.MOD_ID, value = Dist.CLIENT)
public final class TutorialModClient {
    private TutorialModClient() {}

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MACHINE.get(), MachineScreen::new);
    }

    @SubscribeEvent
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PIG_SPAWNER.get(), PigSpawnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COLOR_CONTROLLER.get(), ColorControllerRenderer::new);
    }
}
