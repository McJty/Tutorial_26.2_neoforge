package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.concurrent.atomic.AtomicInteger;

public final class PigSpawnerBlockEntity extends MachineBlockEntity {
    public static final int CAPACITY = GeneratorBlockEntity.GENERATION_RATE * 20 * 10;
    private static final AtomicInteger NEXT_PREVIEW_ID = new AtomicInteger(-1);

    private boolean previewReady;
    private Pig previewPig;

    public final EnergyHandler input = energy;

    public PigSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.PIG_SPAWNER.get(),
                pos,
                state,
                CAPACITY,
                GeneratorBlockEntity.GENERATION_RATE,
                0
        );
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return stack.is(ItemTags.PIG_FOOD);
    }

    private boolean hasPreviewResources() {
        return energy.getEnergyStored() > 0 && accepts(inventory.getStackInSlot(0));
    }

    public boolean shouldShowPreview() {
        return level != null && level.isClientSide() ? previewReady : hasPreviewResources();
    }

    public Pig getPreviewPig() {
        if (previewPig == null && level != null && level.isClientSide()) {
            // The vanilla pig renderer draws visible invisible mobs with translucent blending.
            previewPig = new Pig(EntityTypes.PIG, level) {
                @Override
                public boolean isInvisibleTo(Player player) {
                    return false;
                }
            };
            // Client-side entities receive ID 0 until added to the world. This preview
            // stays outside the world, but the 26.2 renderer requires an assigned ID.
            previewPig.setId(NEXT_PREVIEW_ID.getAndDecrement());
            previewPig.setInvisible(true);
        }
        return previewPig;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel server) {
            boolean ready = hasPreviewResources();
            if (ready != previewReady) {
                previewReady = ready;
                server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("preview_ready", hasPreviewResources());
        return tag;
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        previewReady = input.getBooleanOr("preview_ready", false);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        previewReady = input.getBooleanOr("preview_ready", false);
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel server) || energy.getEnergyStored() != CAPACITY
                || !accepts(inventory.getStackInSlot(0))) {
            return;
        }

        var pig = EntityTypes.PIG.create(server, EntitySpawnReason.SPAWNER);
        if (pig == null) {
            return;
        }

        pig.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
        pig.setYRot(server.getRandom().nextFloat() * 360);
        if (!server.noCollision(pig) || server.containsAnyLiquid(pig.getBoundingBox())
                || !server.getEntities(pig, pig.getBoundingBox()).isEmpty()) {
            return;
        }

        if (server.addFreshEntity(pig)) {
            inventory.extractItem(0, 1, false);
            setEnergy(0);
        }
    }
}
