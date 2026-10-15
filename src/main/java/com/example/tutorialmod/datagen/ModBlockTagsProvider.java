package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;


import java.util.concurrent.CompletableFuture;

public final class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup
    ) {
        super(output, lookup, TutorialMod.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "generator")),
                ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "pig_spawner")),
                ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "teleporter_base")),
                ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "receiver_base")),
                ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "teleporter")),
                ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "destination_controller")),
                ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "receiver")));
    }
}
