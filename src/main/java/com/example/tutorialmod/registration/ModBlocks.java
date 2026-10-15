package com.example.tutorialmod.registration;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.TutorialBlock;
import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.block.ColorControllerBlock;
import com.example.tutorialmod.block.TeleporterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.example.tutorialmod.block.GeneratorBlock;
import com.example.tutorialmod.block.PigSpawnerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TutorialMod.MOD_ID);

    public static final DeferredBlock<TutorialBlock> TUTORIAL_BLOCK = BLOCKS.registerBlock(
            "tutorial_block",
            TutorialBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<GeneratorBlock> GENERATOR = BLOCKS.registerBlock(
            "generator", GeneratorBlock::new,
            properties -> properties.mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)
    );

    public static final DeferredBlock<PigSpawnerBlock> PIG_SPAWNER = BLOCKS.registerBlock(
            "pig_spawner", PigSpawnerBlock::new,
            properties -> properties.mapColor(MapColor.METAL).strength(3.0F)
                    .sound(SoundType.METAL).noOcclusion()
    );

    public static final DeferredBlock<MultiblockBlock> TELEPORTER_BASE = BLOCKS.registerBlock(
            "teleporter_base", MultiblockBlock::new, ModBlocks::multiblockProperties);
    public static final DeferredBlock<MultiblockBlock> RECEIVER_BASE = BLOCKS.registerBlock(
            "receiver_base", MultiblockBlock::new, ModBlocks::multiblockProperties);
    public static final DeferredBlock<TeleporterBlock> TELEPORTER = BLOCKS.registerBlock(
            "teleporter", TeleporterBlock::new, ModBlocks::multiblockProperties);
    public static final DeferredBlock<ColorControllerBlock> DESTINATION_CONTROLLER = BLOCKS.registerBlock(
            "destination_controller", properties -> new ColorControllerBlock(properties, false), ModBlocks::multiblockProperties);
    public static final DeferredBlock<ColorControllerBlock> RECEIVER = BLOCKS.registerBlock(
            "receiver", properties -> new ColorControllerBlock(properties, true), ModBlocks::multiblockProperties);

    private static BlockBehaviour.Properties multiblockProperties(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.METAL).strength(3.0F)
                .sound(SoundType.METAL).noOcclusion();
    }

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
