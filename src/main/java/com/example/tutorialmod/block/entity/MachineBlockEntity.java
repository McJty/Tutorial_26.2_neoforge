package com.example.tutorialmod.block.entity;

import com.example.tutorialmod.menu.MachineMenu;
import com.example.tutorialmod.registration.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.component.DataComponentMap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider {
    private final int capacity;
    public final MachineEnergy energy;

    public final MachineInventory inventory = new MachineInventory() {
        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return accepts(resource.toStack());
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack stack) {
            setChanged();
        }
    };

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                case 2 -> MachineBlockEntity.this instanceof GeneratorBlockEntity generator ? generator.getBurnTicks() : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    protected MachineBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int capacity,
            int receive,
            int extract
    ) {
        super(type, pos, state);
        this.capacity = capacity;
        energy = new MachineEnergy(capacity, receive, extract);
    }

    public void setEnergy(int amount) {
        energy.set(Math.clamp(amount, 0, capacity));
    }

    public abstract boolean accepts(ItemStack stack);

    public abstract void serverTick();

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MachineMenu(id, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", energy.getEnergyStored());
        inventory.serialize(output.child("inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        setEnergy(input.getIntOr("energy", 0));
        inventory.deserialize(input.childOrEmpty("inventory"));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.ENERGY.get(), energy.getEnergyStored());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        setEnergy(components.getOrDefault(ModDataComponents.ENERGY.get(), 0));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("energy");
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Block.popResource(level, pos, inventory.getStackInSlot(0));
        }
        super.preRemoveSideEffects(pos, state);
    }

    public class MachineEnergy extends SimpleEnergyHandler {
        private MachineEnergy(int capacity, int receive, int extract) {
            super(capacity, receive, extract);
        }

        @Override
        protected void onEnergyChanged(int previous) {
            setChanged();
        }

        public int getEnergyStored() {
            return getAmountAsInt();
        }

        public int getMaxEnergyStored() {
            return getCapacityAsInt();
        }

        public int extractEnergy(int amount, boolean simulate) {
            try (var transaction = Transaction.openRoot()) {
                int extracted = extract(amount, transaction);
                if (!simulate) transaction.commit();
                return extracted;
            }
        }

        public int receiveEnergy(int amount, boolean simulate) {
            try (var transaction = Transaction.openRoot()) {
                int received = insert(amount, transaction);
                if (!simulate) transaction.commit();
                return received;
            }
        }
    }

    public abstract class MachineInventory extends ItemStacksResourceHandler {
        private MachineInventory() {
            super(1);
        }

        public ItemStack getStackInSlot(int slot) {
            return getResource(slot).toStack(getAmountAsInt(slot));
        }

        public void setStackInSlot(int slot, ItemStack stack) {
            set(slot, ItemResource.of(stack), stack.getCount());
        }

        public ItemStack extractItem(int slot, int count, boolean simulate) {
            ItemResource resource = getResource(slot);
            try (var transaction = Transaction.openRoot()) {
                int extracted = extract(slot, resource, count, transaction);
                if (!simulate) transaction.commit();
                return resource.toStack(extracted);
            }
        }
    }
}
