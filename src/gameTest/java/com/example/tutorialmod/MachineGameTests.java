package com.example.tutorialmod;

import com.example.tutorialmod.block.GeneratorBlock;
import com.example.tutorialmod.block.entity.GeneratorBlockEntity;
import com.example.tutorialmod.block.entity.MachineBlockEntity;
import com.example.tutorialmod.block.entity.PigSpawnerBlockEntity;
import com.example.tutorialmod.registration.ModBlocks;
import com.example.tutorialmod.registration.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Consumer;

@EventBusSubscriber(modid = TutorialMod.MOD_ID)
public final class MachineGameTests {
    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "machine_environment"),
                new TestEnvironmentDefinition.AllOf());
        register(event, environment, "charges_for_ten_seconds", 220);
        register(event, environment, "requires_food_power_and_space", 100);
        register(event, environment, "input_limit_applies_per_push", 100);
        register(event, environment, "loot_and_save_preserve_energy", 100);
        register(event, environment, "pushes_to_every_neighbour", 20);
        register(event, environment, "test_instance_serializes", 100);
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registrations -> {
            registrations.register(functionId("charges_for_ten_seconds"), MachineGameTests::chargesForTenSeconds);
            registrations.register(functionId("requires_food_power_and_space"), MachineGameTests::requiresFoodPowerAndSpace);
            registrations.register(functionId("input_limit_applies_per_push"), MachineGameTests::inputLimitAppliesPerPush);
            registrations.register(functionId("loot_and_save_preserve_energy"), MachineGameTests::lootAndSavePreserveEnergy);
            registrations.register(functionId("pushes_to_every_neighbour"), MachineGameTests::pushesToEveryNeighbour);
            registrations.register(functionId("test_instance_serializes"), MachineGameTests::testInstanceSerializes);
        });
    }

    private static ResourceKey<Consumer<GameTestHelper>> functionId(String name) {
        return ResourceKey.create(Registries.TEST_FUNCTION,
                Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name));
    }

    private static void register(RegisterGameTestsEvent event,
                                 net.minecraft.core.Holder<TestEnvironmentDefinition<?>> environment,
                                 String name, int ticks) {
        TestData<net.minecraft.core.Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "empty"),
                ticks, 0, true, Rotation.NONE);
        event.registerTest(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name),
                new FunctionGameTestInstance(functionId(name), data));
    }

    private static int push(EnergyHandler handler, int amount, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            int inserted = handler.insert(amount, transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    private static int pull(EnergyHandler handler, int amount) {
        try (var transaction = Transaction.openRoot()) {
            int extracted = handler.extract(amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    public static void testInstanceSerializes(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);
        var operations = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
        for (String name : new String[]{"charges_for_ten_seconds", "requires_food_power_and_space",
                "input_limit_applies_per_push", "loot_and_save_preserve_energy",
                "pushes_to_every_neighbour"}) {
            GameTestInstance instance = registry.getValue(Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name));
            helper.assertTrue(instance != null, "GameTest instance is registered: " + name);
            helper.assertTrue(GameTestInstance.DIRECT_CODEC.encodeStart(operations, instance).result().isPresent(),
                    "GameTest instance is serializable: " + name);
        }
        helper.succeed();
    }

    public static void chargesForTenSeconds(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, ModBlocks.GENERATOR.get());
        helper.setBlock(3, 1, 2, ModBlocks.PIG_SPAWNER.get());
        GeneratorBlockEntity generator = helper.getBlockEntity(new BlockPos(2, 1, 2), GeneratorBlockEntity.class);
        PigSpawnerBlockEntity spawner = helper.getBlockEntity(new BlockPos(3, 1, 2), PigSpawnerBlockEntity.class);
        generator.inventory.setStackInSlot(0, new ItemStack(Items.COAL));
        spawner.inventory.setStackInSlot(0, new ItemStack(Items.CARROT, 2));
        helper.runAfterDelay(199, () -> {
            helper.assertEntityNotPresent(EntityTypes.PIG);
            helper.assertTrue(
                    spawner.energy.getEnergyStored() >= 3960,
                    "Buffer should be nearly full after 199 ticks"
            );
        });
        helper.runAfterDelay(201, () -> {
            helper.assertEntitiesPresent(EntityTypes.PIG, 1);
            helper.assertTrue(spawner.inventory.getStackInSlot(0).getCount() == 1, "One food per pig");
            helper.assertTrue(spawner.energy.getEnergyStored() <= 40, "Spawning empties the buffer");
            helper.succeed();
        });
    }

    public static void requiresFoodPowerAndSpace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.PIG_SPAWNER.get());
        PigSpawnerBlockEntity spawner = helper.getBlockEntity(pos, PigSpawnerBlockEntity.class);
        spawner.setEnergy(PigSpawnerBlockEntity.CAPACITY);
        spawner.serverTick();
        helper.assertEntityNotPresent(EntityTypes.PIG);
        helper.assertTrue(!spawner.shouldShowPreview(), "Power alone should not show a pig preview");
        helper.assertTrue(spawner.energy.getEnergyStored() == 4000, "No food must preserve energy");
        spawner.inventory.setStackInSlot(0, new ItemStack(Items.CARROT));
        spawner.setEnergy(3999);
        spawner.serverTick();
        helper.assertEntityNotPresent(EntityTypes.PIG);
        helper.assertTrue(spawner.shouldShowPreview(), "Food and partial power show a pig preview");
        helper.assertTrue(spawner.getUpdateTag(helper.getLevel().registryAccess()).getBoolean("preview_ready").orElse(false),
                "The preview state is sent to clients");
        helper.assertTrue(
                spawner.inventory.getStackInSlot(0).getCount() == 1,
                "Partial energy must preserve food"
        );
        spawner.setEnergy(4000);
        helper.setBlock(pos.above(), Blocks.STONE);
        spawner.serverTick();
        helper.assertEntityNotPresent(EntityTypes.PIG);
        helper.assertTrue(spawner.energy.getEnergyStored() == 4000, "Blocked spawn must preserve energy");
        helper.assertTrue(spawner.shouldShowPreview(), "The preview remains while spawning is blocked");
        helper.setBlock(pos.above(), Blocks.AIR);
        spawner.serverTick();
        helper.assertEntitiesPresent(EntityTypes.PIG, 1);
        helper.assertTrue(!spawner.shouldShowPreview(), "The preview disappears when the pig spawns");
        var absolutePos = helper.absolutePos(pos);
        var pigs = helper.getLevel().getEntities(
                EntityTypes.PIG,
                new AABB(absolutePos).inflate(2),
                pig -> true
        );
        helper.assertTrue(
                pigs.size() == 1 && Math.abs(pigs.getFirst().getY() - (absolutePos.getY() + 0.5)) < 0.01,
                "Pig should stand on the half-height platform"
        );
        helper.assertTrue(
                spawner.getBlockState().getCollisionShape(helper.getLevel(), absolutePos).max(Direction.Axis.Y) == 0.5,
                "Spawner collision should match a bottom slab"
        );
        helper.assertTrue(
                spawner.energy.getEnergyStored() == 0 && spawner.inventory.getStackInSlot(0).isEmpty(),
                "Successful spawn consumes both resources"
        );
        helper.succeed();
    }

    public static void inputLimitAppliesPerPush(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.PIG_SPAWNER.get());
        var level = helper.getLevel();
        var north = level.getCapability(
                Capabilities.Energy.BLOCK,
                helper.absolutePos(pos),
                Direction.NORTH
        );
        var south = level.getCapability(
                Capabilities.Energy.BLOCK,
                helper.absolutePos(pos),
                Direction.SOUTH
        );
        helper.assertTrue(north != null && south != null, "Energy capability on each side");
        helper.assertTrue(push(north, 1000, true) == 20, "Simulated transfer is capped");
        helper.assertTrue(push(north, 1000, false) == 20, "Simulation does not transfer power");
        helper.assertTrue(push(north, 1000, false) == 20, "A second push can also transfer power");
        helper.assertTrue(push(south, 1000, false) == 20, "Another side can transfer power too");
        PigSpawnerBlockEntity spawner = helper.getBlockEntity(pos, PigSpawnerBlockEntity.class);
        helper.assertTrue(spawner.energy.getEnergyStored() == 60, "Three pushes each add 20 power");
        helper.assertTrue(pull(south, 1000) == 0, "Spawner does not export power");
        helper.succeed();
    }

    public static void lootAndSavePreserveEnergy(GameTestHelper helper) {
        for (Block block : new Block[]{ModBlocks.GENERATOR.get(), ModBlocks.PIG_SPAWNER.get()}) {
            BlockPos pos = new BlockPos(2, 1, 2);
            helper.setBlock(pos, block);
            MachineBlockEntity machine = helper.getBlockEntity(pos, MachineBlockEntity.class);
            machine.setEnergy(1234);
            machine.inventory.setStackInSlot(
                    0,
                    new ItemStack(block == ModBlocks.GENERATOR.get() ? Items.COAL : Items.CARROT, 3)
            );
            var saved = machine.saveWithoutMetadata(helper.getLevel().registryAccess());
            var drops = Block.getDrops(
                    machine.getBlockState(),
                    helper.getLevel(),
                    helper.absolutePos(pos),
                    machine
            );
            helper.assertTrue(drops.size() == 1 && drops.getFirst().getOrDefault(ModDataComponents.ENERGY.get(), 0) == 1234,
                    "Generated loot copies stored energy");
            helper.setBlock(pos, Blocks.AIR);
            helper.setBlock(pos, block);
            MachineBlockEntity restored = helper.getBlockEntity(pos, MachineBlockEntity.class);
            restored.applyComponentsFromItemStack(drops.getFirst());
            helper.assertTrue(
                    restored.energy.getEnergyStored() == 1234,
                    "Placement component hook restores energy"
            );
            restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,
                    helper.getLevel().registryAccess(), saved));
            helper.assertTrue(restored.energy.getEnergyStored() == 1234 && restored.inventory.getStackInSlot(0).getCount() == 3,
                    "World save restores inventory and energy");
        }

        helper.succeed();
    }

    public static void pushesToEveryNeighbour(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 2, 3);
        helper.setBlock(center, ModBlocks.GENERATOR.get());
        GeneratorBlockEntity generator = helper.getBlockEntity(center, GeneratorBlockEntity.class);
        generator.inventory.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS));
        generator.setEnergy(GeneratorBlockEntity.GENERATION_RATE * Direction.values().length);
        for (Direction side : Direction.values()) {
            helper.setBlock(center.relative(side), ModBlocks.PIG_SPAWNER.get());
        }

        helper.runAfterDelay(1, () -> {
            for (Direction side : Direction.values()) {
                PigSpawnerBlockEntity consumer = helper.getBlockEntity(center.relative(side), PigSpawnerBlockEntity.class);
                helper.assertTrue(
                        consumer.energy.getEnergyStored() == GeneratorBlockEntity.GENERATION_RATE,
                        "Every neighbour receives a full transfer: " + side
                );
            }

            helper.assertTrue(
                    generator.getBlockState().getValue(GeneratorBlock.ON),
                    "Fuel turns on the generator"
            );
            helper.succeed();
        });
    }
}
