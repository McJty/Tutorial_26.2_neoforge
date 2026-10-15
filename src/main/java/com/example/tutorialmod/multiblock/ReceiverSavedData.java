package com.example.tutorialmod.multiblock;

import com.example.tutorialmod.block.entity.ColorControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stored in the Overworld, so every dimension shares the same receiver directory. */
public final class ReceiverSavedData extends SavedData {
    private record SavedReceiver(GlobalPos pos, ReceiverColors colors) {
        private static final Codec<SavedReceiver> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(entry -> entry.pos.dimension()),
                Codec.LONG.fieldOf("pos").forGetter(entry -> entry.pos.pos().asLong()),
                DyeColor.LEGACY_ID_CODEC.optionalFieldOf("color1", DyeColor.WHITE).forGetter(entry -> entry.colors.color1()),
                DyeColor.LEGACY_ID_CODEC.optionalFieldOf("color2", DyeColor.WHITE).forGetter(entry -> entry.colors.color2()),
                DyeColor.LEGACY_ID_CODEC.optionalFieldOf("color3", DyeColor.WHITE).forGetter(entry -> entry.colors.color3())
        ).apply(instance, (dimension, pos, first, second, third) -> new SavedReceiver(
                GlobalPos.of(dimension, BlockPos.of(pos)), new ReceiverColors(first, second, third))));
    }

    public static final Codec<ReceiverSavedData> CODEC = SavedReceiver.CODEC.listOf().fieldOf("receivers")
            .codec().xmap(ReceiverSavedData::new, data -> data.receivers.entrySet().stream()
                    .map(entry -> new SavedReceiver(entry.getKey(), entry.getValue())).toList());
    private static final SavedDataType<ReceiverSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("tutorialmod", "receivers"), ReceiverSavedData::new, CODEC);
    private final Map<GlobalPos, ReceiverColors> receivers = new HashMap<>();

    // Keep a snapshot of the receiver's colors, rather than a reference to its mutable BE.
    private record ReceiverColors(DyeColor color1, DyeColor color2, DyeColor color3) {
    }

    public ReceiverSavedData() {
    }

    private ReceiverSavedData(List<SavedReceiver> entries) {
        entries.forEach(entry -> receivers.put(entry.pos(), entry.colors()));
    }

    public static ReceiverSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void setReceiver(GlobalPos pos, DyeColor color1, DyeColor color2, DyeColor color3) {
        ReceiverColors colors = new ReceiverColors(color1, color2, color3);
        ReceiverColors previous = receivers.put(pos, colors);
        if (!colors.equals(previous)) {
            setDirty();
        }
    }

    public void removeReceiver(GlobalPos pos) {
        if (receivers.remove(pos) != null) {
            setDirty();
        }
    }

    public GlobalPos randomReceiver(ServerLevel source, DyeColor color1, DyeColor color2, DyeColor color3) {
        ReceiverColors wanted = new ReceiverColors(color1, color2, color3);
        List<GlobalPos> candidates = new ArrayList<>();
        receivers.forEach((pos, colors) -> {
            if (colors.equals(wanted)) {
                candidates.add(pos);
            }
        });
        while (!candidates.isEmpty()) {
            GlobalPos destination = candidates.remove(source.getRandom().nextInt(candidates.size()));
            ServerLevel target = source.getServer().getLevel(destination.dimension());
            if (target == null) {
                continue;
            }
            // Load only a selected candidate, rather than requiring all receivers to stay loaded
            target.getChunkAt(destination.pos());
            if (target.getBlockEntity(destination.pos()) instanceof ColorControllerBlockEntity receiver
                    && receiver.isReceiver()) {
                ReceiverColors actual = new ReceiverColors(receiver.getColor1(), receiver.getColor2(), receiver.getColor3());
                if (actual.equals(wanted)) {
                    return destination;
                }
                setReceiver(destination, receiver.getColor1(), receiver.getColor2(), receiver.getColor3());
            } else {
                // World edits can leave stale entries. Repair them when encountered
                removeReceiver(destination);
            }
        }
        return null;
    }
}
