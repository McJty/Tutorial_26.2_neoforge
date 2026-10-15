package com.example.tutorialmod.datagen;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.multiblock.MultiblockPart;
import com.example.tutorialmod.multiblock.MultiblockShapes;
import com.example.tutorialmod.registration.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.function.Consumer;

import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

/** Adds multiblock models to the main ModelProvider's shared output and validation. */
public final class ModMultiblockModelProvider {
    private static final TextureSlot PAD = TextureSlot.create("pad");
    private static final TextureSlot SLOT = TextureSlot.create("slot");
    private static final TextureSlot FIRST_SLOT = TextureSlot.create("first_slot");
    private final BlockModelGenerators blocks;
    private final ItemModelGenerators items;

    public ModMultiblockModelProvider(BlockModelGenerators blocks, ItemModelGenerators items) {
        this.blocks = blocks;
        this.items = items;
    }

    public void registerModels() {
        Identifier platform = piece("multiblock_platform", model -> cuboid(model, 0, 0, 0, 16, 8, 16, TextureSlot.ALL));
        Identifier corner = piece("multiblock_corner", this::corner);
        Identifier pillarBase = piece("multiblock_pillar_base", model -> {
            corner(model);
            cuboid(model, 6, 8, 6, 10, 16, 10, TextureSlot.ALL);
        });
        Identifier pillar = piece("multiblock_pillar", model -> cuboid(model, 6, 0, 6, 10, 16, 10, TextureSlot.ALL));
        Identifier upperPillar = piece("multiblock_upper_pillar", model -> {
            cuboid(model, 6, 0, 6, 10, 16, 10, TextureSlot.ALL);
            roofQuarter(model);
        });
        registerBase(ModBlocks.TELEPORTER_BASE.get(), "block/lapis_block", platform, pillarBase, pillar, upperPillar);
        registerBase(ModBlocks.RECEIVER_BASE.get(), "block/copper_block", platform, corner, pillar, upperPillar);

        Identifier teleporter = piece("teleporter", metal().put(PAD, material("block/purple_concrete")), model -> {
            model.requiredTextureSlot(PAD);
            cuboid(model, 0, 0, 0, 16, 8, 16, TextureSlot.ALL);
            cuboid(model, 5, 8, 2, 11, 8.1F, 14, PAD);
            cuboid(model, 3, 8, 3, 13, 8.1F, 13, PAD);
            cuboid(model, 2, 8, 5, 14, 8.1F, 11, PAD);
        });
        registerSpecial(ModBlocks.TELEPORTER.get(), teleporter);
        registerSpecial(ModBlocks.DESTINATION_CONTROLLER.get(), controller("destination_controller", false));
        registerSpecial(ModBlocks.RECEIVER.get(), controller("receiver", true));
    }

    private Identifier controller(String name, boolean receiver) {
        return piece(name, metal().put(SLOT, material("block/polished_deepslate"))
                .put(FIRST_SLOT, material("block/gold_block")), model -> {
            model.requiredTextureSlot(SLOT).requiredTextureSlot(FIRST_SLOT);
            cuboid(model, 0, 0, 0, 16, 8, 16, TextureSlot.ALL);
            for (int slot = 0; slot < 3; slot++) {
                int x = (3 - slot) * 4;
                TextureSlot frame = slot == 0 ? FIRST_SLOT : SLOT;
                if (receiver) {
                    cuboid(model, x - 2, 8, 6, x + 2, 8.1F, 10, frame);
                } else {
                    cuboid(model, x - 2, 2, -0.1F, x + 2, 6, 0, frame);
                }
            }
        });
    }

    private void corner(ExtendedModelTemplateBuilder model) {
        for (int strip = 0; strip < 4; strip++) {
            cuboid(model, 12 - strip * 4, 0, strip * 4, 16, 8, strip * 4 + 4, TextureSlot.ALL);
        }
    }

    private Identifier piece(String name, Consumer<ExtendedModelTemplateBuilder> geometry) {
        return piece(name, metal(), geometry);
    }

    private Identifier piece(String name, TextureMapping textures, Consumer<ExtendedModelTemplateBuilder> geometry) {
        var template = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.ALL).requiredTextureSlot(TextureSlot.PARTICLE);
        geometry.accept(template);
        return template.build().create(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/" + name),
                textures, blocks.modelOutput);
    }

    private void registerBase(Block block, String top, Identifier platform, Identifier corner,
                              Identifier pillar, Identifier upperPillar) {
        Identifier construction = ModelTemplates.CUBE_BOTTOM_TOP.create(block, new TextureMapping()
                .put(TextureSlot.SIDE, material("block/iron_block"))
                .put(TextureSlot.BOTTOM, material("block/iron_block"))
                .put(TextureSlot.TOP, material(top)), blocks.modelOutput);
        var dispatch = PropertyDispatch.initial(MultiblockBlock.FORMED, MultiblockBlock.PART, MultiblockBlock.FACING);
        for (boolean formed : new boolean[]{false, true}) {
            for (MultiblockPart part : MultiblockPart.values()) {
                Identifier model = !formed ? construction : switch (part) {
                    case PLATFORM -> platform;
                    case CORNER -> corner;
                    case PILLAR -> pillar;
                    case UPPER_PILLAR -> upperPillar;
                };
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                    dispatch.select(formed, part, facing, rotated(model, facing));
                }
            }
        }
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
        items.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(construction));
    }

    private void registerSpecial(Block block, Identifier model) {
        var dispatch = PropertyDispatch.initial(MultiblockBlock.FACING);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            dispatch.select(facing, rotated(model, facing));
        }
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private static MultiVariant rotated(Identifier model, Direction facing) {
        return switch (MultiblockShapes.turns(facing)) {
            case 1 -> plainVariant(model).with(BlockModelGenerators.Y_ROT_90);
            case 2 -> plainVariant(model).with(BlockModelGenerators.Y_ROT_180);
            case 3 -> plainVariant(model).with(BlockModelGenerators.Y_ROT_270);
            default -> plainVariant(model);
        };
    }

    private void roofQuarter(ExtendedModelTemplateBuilder model) {
        // Four quarters fill the consumed 3x3 roof; local UVs keep the texture at normal scale.
        for (int x = 0; x < 24; x += 16) {
            for (int z = 0; z < 24; z += 16) {
                int width = Math.min(16, 24 - x);
                int depth = Math.min(16, 24 - z);
                int startX = x;
                int startZ = z;
                model.element(element -> element.from(startX, 16, startZ).to(startX + width, 20, startZ + depth)
                        .allFaces((side, face) -> {
                            face.texture(TextureSlot.ALL);
                            switch (side) {
                                case UP -> face.uvs(0, 0, width, depth);
                                case DOWN -> face.uvs(0, 16 - depth, width, 16);
                                case NORTH -> face.uvs(16 - width, 12, 16, 16);
                                case SOUTH -> face.uvs(0, 12, width, 16);
                                case WEST -> face.uvs(0, 12, depth, 16);
                                case EAST -> face.uvs(16 - depth, 12, 16, 16);
                            }
                        }));
            }
        }
    }

    private static void cuboid(ExtendedModelTemplateBuilder model, float x1, float y1, float z1,
                               float x2, float y2, float z2, TextureSlot texture) {
        model.element(element -> element.from(x1, y1, z1).to(x2, y2, z2).allFaces((side, face) -> face.texture(texture)));
    }

    private static TextureMapping metal() {
        return new TextureMapping().put(TextureSlot.ALL, material("block/iron_block"))
                .put(TextureSlot.PARTICLE, material("block/iron_block"));
    }

    private static Material material(String texture) {
        return new Material(Identifier.withDefaultNamespace(texture));
    }
}
