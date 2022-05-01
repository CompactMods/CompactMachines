package dev.compactmods.machines.room.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.core.LevelBlockPosition;

public record PlayerRoomHistoryItem(LevelBlockPosition entry, int machine) implements IRoomHistoryItem {

    public static final Codec<PlayerRoomHistoryItem> CODEC = RecordCodecBuilder.create(i -> i.group(
            LevelBlockPosition.CODEC.fieldOf("position").forGetter(PlayerRoomHistoryItem::entry),
            Codec.INT.fieldOf("machine").forGetter(IRoomHistoryItem::getMachine)
    ).apply(i, PlayerRoomHistoryItem::new));

    @Override
    public IDimensionalPosition getEntryLocation() {
        return entry;
    }

    @Override
    public int getMachine() {
        return machine;
    }
}
