package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public final class ModBlockStateProvider extends ModelProvider {
    public ModBlockStateProvider(PackOutput output) {
        super(output, TutorialMod.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Material tutorialBlockTexture = new Material(
                Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/block_texture")
        );
        blockModels.createTrivialBlock(
                ModBlocks.TUTORIAL_BLOCK.get(),
                block -> TexturedModel.createAllSame(tutorialBlockTexture)
        );
    }
}
