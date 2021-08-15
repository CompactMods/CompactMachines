package dev.compactmods.machines.rooms.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.teleportation.DimensionalPosition;

public class PlayerRoomHistoryItem implements IRoomHistoryItem {

    private final DimensionalPosition entry;
    private final int machine;

    public static final Codec<IRoomHistoryItem> CODEC = RecordCodecBuilder.create(i -> i.group(
            DimensionalPosition.CODEC.fieldOf("position").forGetter(IRoomHistoryItem::getEntryLocation),
            Codec.INT.fieldOf("machine").forGetter(IRoomHistoryItem::getMachine)
    ).apply(i, PlayerRoomHistoryItem::new));

    public PlayerRoomHistoryItem(DimensionalPosition entry, int machine) {
        this.entry = entry;
        this.machine = machine;
    }

    @Override
    public DimensionalPosition getEntryLocation() {
        return entry;
    }

    @Override
    public int getMachine() {
        return machine;
    }
}
