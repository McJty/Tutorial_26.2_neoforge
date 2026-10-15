package com.example.tutorialmod.datagen;

import com.example.tutorialmod.registration.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider.Runner {
    public ModRecipeProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(output, lookupProvider);
    }

    @Override
    protected RecipeProvider createRecipeProvider(
            HolderLookup.Provider registries,
            RecipeOutput output
    ) {
        return new Provider(registries, output);
    }

    @Override
    public String getName() {
        return "Tutorial Mod Recipes";
    }

    private static final class Provider extends RecipeProvider {
        private Provider(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        protected void buildRecipes() {
            shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.TELEPORTER_BASE.get(), 8)
                    .pattern("III").pattern("IEI").pattern("III")
                    .define('I', Items.IRON_INGOT).define('E', Items.ENDER_PEARL)
                    .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL)).save(output);
            shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.RECEIVER_BASE.get(), 8)
                    .pattern("III").pattern("IRI").pattern("III")
                    .define('I', Items.IRON_INGOT).define('R', Items.REDSTONE)
                    .unlockedBy("has_redstone", has(Items.REDSTONE)).save(output);
            shaped(RecipeCategory.REDSTONE, ModItems.GENERATOR.get())
                    .pattern("III").pattern("IFI").pattern("IRI")
                    .define('I', Items.IRON_INGOT).define('F', Items.FURNACE)
                    .define('R', Items.REDSTONE)
                    .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT)).save(output);
            shaped(RecipeCategory.REDSTONE, ModItems.PIG_SPAWNER.get())
                    .pattern("B B").pattern("ICI").pattern("IRI")
                    .define('B', Items.IRON_BARS).define('I', Items.IRON_INGOT)
                    .define('C', Items.CARROT).define('R', Items.REDSTONE)
                    .unlockedBy("has_carrot", has(Items.CARROT)).save(output);
            shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.TUTORIAL_BLOCK_ITEM.get())
                    .pattern("III")
                    .pattern("ISI")
                    .pattern("III")
                    .define('I', Items.IRON_INGOT)
                    .define('S', Items.STONE)
                    .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                    .save(output);
        }
    }
}
