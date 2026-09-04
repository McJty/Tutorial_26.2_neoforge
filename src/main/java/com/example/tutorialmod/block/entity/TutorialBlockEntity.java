package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.block.TutorialBlock;
import com.example.tutorialmod.registration.ModBlockEntities;
import com.example.tutorialmod.registration.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TutorialBlockEntity extends BlockEntity {
    private static final String TAG_IS_ON = "is_on";

    private boolean isOn;

    public TutorialBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.TUTORIAL_BLOCK_ENTITY.get(), position, state);
        isOn = state.getValue(TutorialBlock.ON);
    }

    public boolean isOn() {
        return isOn;
    }

    public void toggle() {
        setOn(!isOn);
    }

    public void setOn(boolean isOn) {
        if (this.isOn == isOn) {
            return;
        }

        this.isOn = isOn;
        setChanged();

        if (level != null && getBlockState().getValue(TutorialBlock.ON) != isOn) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(TutorialBlock.ON, isOn),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        isOn = input.getBooleanOr(TAG_IS_ON, false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(TAG_IS_ON, isOn);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        // This is copied to the dropped ItemStack by the generated loot table.
        components.set(ModDataComponents.IS_ON.get(), isOn);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        // BlockItem calls this after it creates the new block entity.
        setOn(components.getOrDefault(ModDataComponents.IS_ON.get(), false));
    }

    @SuppressWarnings("deprecation") // This is the component transfer hook in Minecraft 26.2.
    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(TAG_IS_ON);
    }
}
