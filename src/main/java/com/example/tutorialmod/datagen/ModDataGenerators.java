package com.example.tutorialmod.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModBlockStateProvider::new);
        event.createProvider(ModLanguageProvider::new);
        event.createProvider(ModGuiTextureProvider::new);
    }

    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(ModBlockLootTableProvider::new);
        event.createProvider(ModRecipeProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
    }
}
