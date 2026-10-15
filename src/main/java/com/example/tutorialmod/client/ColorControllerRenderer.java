package com.example.tutorialmod.client;

import com.example.tutorialmod.block.MultiblockBlock;
import com.example.tutorialmod.block.entity.ColorControllerBlockEntity;
import com.example.tutorialmod.block.ColorControllerBlock;
import com.example.tutorialmod.multiblock.MultiblockShapes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** Tiny wool cubes show the three colors. World data is copied during extraction. */
public final class ColorControllerRenderer implements BlockEntityRenderer<ColorControllerBlockEntity, ColorControllerRenderer.State> {
    private final BlockModelResolver blocks;

    public ColorControllerRenderer(BlockEntityRendererProvider.Context context) {
        blocks = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ColorControllerBlockEntity entity, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breakProgress);
        state.turns = MultiblockShapes.turns(entity.getBlockState().getValue(MultiblockBlock.FACING));
        state.receiver = entity.isReceiver();
        for (int slot = 0; slot < 3; slot++) {
            blocks.update(state.colors[slot], ColorControllerBlock.woolBlock(entity.getColor(slot)).defaultBlockState(),
                    BlockDisplayContext.create());
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.mulPose(Axis.YP.rotationDegrees(-90 * state.turns));
        poses.translate(-0.5, 0, -0.5);
        for (int slot = 0; slot < 3; slot++) {
            poses.pushPose();
            double x = (3 - slot) / 4.0 - 1.0 / 16;
            if (state.receiver) {
                poses.translate(x, 0.5, 7.0 / 16);
            } else {
                poses.translate(x, 3.0 / 16, -1.0 / 16);
            }
            poses.scale(0.125F, 0.125F, 0.125F);
            state.colors[slot].submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
        poses.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        private final BlockModelRenderState[] colors = {
                new BlockModelRenderState(), new BlockModelRenderState(), new BlockModelRenderState()
        };
        private int turns;
        private boolean receiver;
    }
}
