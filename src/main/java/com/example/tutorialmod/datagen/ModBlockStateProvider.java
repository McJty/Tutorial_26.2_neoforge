package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.TutorialBlock;
import com.example.tutorialmod.block.GeneratorBlock;
import com.example.tutorialmod.registration.ModBlocks;
import com.example.tutorialmod.registration.ModDataComponents;
import com.example.tutorialmod.registration.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

public final class ModBlockStateProvider extends ModelProvider {
    public ModBlockStateProvider(PackOutput output) {
        super(output, TutorialMod.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Material baseTexture = new Material(
                Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/block_texture")
        );
        Material offTexture = new Material(
                Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/block_texture_off")
        );
        Material onTexture = new Material(
                Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/block_texture_on")
        );

        TextureMapping offTextures = new TextureMapping()
                .put(TextureSlot.SIDE, baseTexture)
                .put(TextureSlot.BOTTOM, baseTexture)
                .put(TextureSlot.TOP, offTexture);
        TextureMapping onTextures = new TextureMapping()
                .put(TextureSlot.SIDE, baseTexture)
                .put(TextureSlot.BOTTOM, baseTexture)
                .put(TextureSlot.TOP, onTexture);

        Identifier offModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithOverride(
                ModBlocks.TUTORIAL_BLOCK.get(),
                "_off",
                offTextures,
                blockModels.modelOutput
        );
        Identifier onModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithOverride(
                ModBlocks.TUTORIAL_BLOCK.get(),
                "_on",
                onTextures,
                blockModels.modelOutput
        );

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(ModBlocks.TUTORIAL_BLOCK.get())
                        .with(
                                PropertyDispatch.initial(TutorialBlock.ON)
                                        .select(false, plainVariant(offModel))
                                        .select(true, plainVariant(onModel))
                        )
        );

        itemModels.itemModelOutput.accept(
                ModItems.TUTORIAL_BLOCK_ITEM.get(),
                ItemModelUtils.select(
                        new ComponentContents<>(ModDataComponents.IS_ON.get()),
                        ItemModelUtils.plainModel(offModel),
                        ItemModelUtils.when(true, ItemModelUtils.plainModel(onModel))
                )
        );

        Material side = new Material(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID,
                "block/block_texture"));
        Material generatorFront = new Material(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID,
                "block/generator"));
        Material generatorOnFront = new Material(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID,
                "block/generator_on"));
        Identifier generatorOff = ModelTemplates.CUBE_ORIENTABLE.createWithOverride(
                ModBlocks.GENERATOR.get(), "", new TextureMapping()
                        .put(TextureSlot.SIDE, side).put(TextureSlot.TOP, side)
                        .put(TextureSlot.FRONT, generatorFront), blockModels.modelOutput);
        Identifier generatorOn = ModelTemplates.CUBE_ORIENTABLE.createWithOverride(
                ModBlocks.GENERATOR.get(), "_on", new TextureMapping()
                        .put(TextureSlot.SIDE, side).put(TextureSlot.TOP, side)
                        .put(TextureSlot.FRONT, generatorOnFront), blockModels.modelOutput);
        var generatorDispatch = PropertyDispatch.initial(GeneratorBlock.FACING, GeneratorBlock.ON);
        generatorDispatch.select(Direction.NORTH, false, plainVariant(generatorOff));
        generatorDispatch.select(Direction.NORTH, true, plainVariant(generatorOn));
        generatorDispatch.select(Direction.EAST, false,
                plainVariant(generatorOff).with(BlockModelGenerators.Y_ROT_90));
        generatorDispatch.select(Direction.EAST, true,
                plainVariant(generatorOn).with(BlockModelGenerators.Y_ROT_90));
        generatorDispatch.select(Direction.SOUTH, false,
                plainVariant(generatorOff).with(BlockModelGenerators.Y_ROT_180));
        generatorDispatch.select(Direction.SOUTH, true,
                plainVariant(generatorOn).with(BlockModelGenerators.Y_ROT_180));
        generatorDispatch.select(Direction.WEST, false,
                plainVariant(generatorOff).with(BlockModelGenerators.Y_ROT_270));
        generatorDispatch.select(Direction.WEST, true,
                plainVariant(generatorOn).with(BlockModelGenerators.Y_ROT_270));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.GENERATOR.get())
                .with(generatorDispatch));
        itemModels.itemModelOutput.accept(ModItems.GENERATOR.get(), ItemModelUtils.plainModel(generatorOff));

        Identifier spawnerModel = Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID,
                "block/pig_spawner");
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                ModBlocks.PIG_SPAWNER.get(), plainVariant(spawnerModel)));
        itemModels.itemModelOutput.accept(ModItems.PIG_SPAWNER.get(),
                ItemModelUtils.plainModel(spawnerModel));
    }
}
