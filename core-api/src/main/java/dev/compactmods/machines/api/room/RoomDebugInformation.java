package dev.compactmods.machines.api.room;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record RoomDebugInformation(String roomCode, UUID owner) {

    public static final Codec<RoomDebugInformation> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("room_code").forGetter(RoomDebugInformation::roomCode),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(RoomDebugInformation::owner)
    ).apply(i, RoomDebugInformation::new));

    public static final StreamCodec<FriendlyByteBuf, RoomDebugInformation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomDebugInformation::roomCode,
            UUIDUtil.STREAM_CODEC, RoomDebugInformation::owner,
            RoomDebugInformation::new
    );
}
