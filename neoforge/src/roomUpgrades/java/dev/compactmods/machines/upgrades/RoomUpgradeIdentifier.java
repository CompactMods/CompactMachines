package dev.compactmods.machines.upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record RoomUpgradeIdentifier(String roomCode, UUID instanceId) {
    public static final Codec<RoomUpgradeIdentifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("room_code").forGetter(RoomUpgradeIdentifier::roomCode),
            UUIDUtil.CODEC.fieldOf("upgrade_id").forGetter(RoomUpgradeIdentifier::instanceId)
    ).apply(instance, RoomUpgradeIdentifier::new));
}
