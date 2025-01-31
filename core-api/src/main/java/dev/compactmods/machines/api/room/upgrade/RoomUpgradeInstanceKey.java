package dev.compactmods.machines.api.room.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record RoomUpgradeInstanceKey(String roomCode, UUID instanceId) {
    public static final Codec<RoomUpgradeInstanceKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("room_code").forGetter(RoomUpgradeInstanceKey::roomCode),
            UUIDUtil.CODEC.fieldOf("upgrade_id").forGetter(RoomUpgradeInstanceKey::instanceId)
    ).apply(instance, RoomUpgradeInstanceKey::new));
}
