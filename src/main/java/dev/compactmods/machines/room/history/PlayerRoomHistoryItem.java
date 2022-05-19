package dev.compactmods.machines.room.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.location.PreciseDimensionalPosition;

public record PlayerRoomHistoryItem(PreciseDimensionalPosition entry, LevelBlockPosition machine) implements IRoomHistoryItem {

    public static final Codec<PlayerRoomHistoryItem> CODEC = RecordCodecBuilder.create(i -> i.group(
            PreciseDimensionalPosition.CODEC.fieldOf("position").forGetter(PlayerRoomHistoryItem::entry),
            LevelBlockPosition.CODEC.fieldOf("machine").forGetter(PlayerRoomHistoryItem::machine)
    ).apply(i, PlayerRoomHistoryItem::new));

    @Override
    public IDimensionalPosition getEntryLocation() {
        return entry;
    }

    @Override
    public IDimensionalBlockPosition getMachine() {
        return machine;
    }
}
