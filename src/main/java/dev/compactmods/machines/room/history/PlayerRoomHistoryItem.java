package dev.compactmods.machines.room.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import net.minecraft.core.GlobalPos;

public record PlayerRoomHistoryItem(PreciseDimensionalPosition entry, GlobalPos machine) implements IRoomHistoryItem {

    public static final Codec<PlayerRoomHistoryItem> CODEC = RecordCodecBuilder.create(i -> i.group(
            PreciseDimensionalPosition.CODEC.fieldOf("position").forGetter(PlayerRoomHistoryItem::entry),
            CodecExtensions.DIMPOS_GLOBALPOS_CODEC.fieldOf("machine").forGetter(PlayerRoomHistoryItem::machine)
    ).apply(i, PlayerRoomHistoryItem::new));

    @Override
    public IDimensionalPosition getEntryLocation() {
        return entry;
    }

    @Override
    public GlobalPos getMachine() {
        return machine;
    }
}
