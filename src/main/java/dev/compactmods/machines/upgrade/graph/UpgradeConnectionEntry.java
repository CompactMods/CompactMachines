package dev.compactmods.machines.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;

public record UpgradeConnectionEntry<T extends RoomUpgrade>(ChunkPos room, ResourceKey<RoomUpgrade> upgradeKey, T instance) {

    public static final Codec<UpgradeConnectionEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.CHUNKPOS
                    .fieldOf("room")
                    .forGetter(UpgradeConnectionEntry::room),

            ResourceKey.codec(RoomUpgrade.REG_KEY)
                    .fieldOf("upgrade")
                    .forGetter(UpgradeConnectionEntry::upgradeKey),

            MachineRoomUpgrades.REGISTRY.get().getCodec()
                    .fieldOf("data").forGetter(UpgradeConnectionEntry::instance)

    ).apply(i, UpgradeConnectionEntry::new));
}
