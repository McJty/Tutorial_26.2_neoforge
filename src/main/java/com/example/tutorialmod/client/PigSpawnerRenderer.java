package com.example.tutorialmod.client;

import com.example.tutorialmod.block.entity.PigSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PigSpawnerRenderer implements BlockEntityRenderer<PigSpawnerBlockEntity, PigSpawnerRenderer.State> {
    private final EntityRenderDispatcher entities;

    public PigSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        entities = context.entityRenderer();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PigSpawnerBlockEntity spawner, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(spawner, state, partialTick, camera, breakProgress);
        state.pig = null;
        if (!spawner.shouldShowPreview()) return;
        var pig = spawner.getPreviewPig();
        if (pig == null) return;
        state.pig = entities.extractEntity(pig, partialTick);
        state.time = spawner.getLevel().getGameTime() + partialTick;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.pig == null) return;
        pose.pushPose();
        pose.translate(0.5, 0.75 + Math.sin(state.time * 0.12) * 0.06, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(state.time * 4.0F));
        pose.scale(0.8F, 0.8F, 0.8F);
        entities.submit(state.pig, camera, 0, 0, 0, pose, collector);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(PigSpawnerBlockEntity spawner) {
        return new AABB(spawner.getBlockPos()).expandTowards(0, 1.5, 0);
    }

    public static final class State extends BlockEntityRenderState {
        private EntityRenderState pig;
        private float time;
    }
}
