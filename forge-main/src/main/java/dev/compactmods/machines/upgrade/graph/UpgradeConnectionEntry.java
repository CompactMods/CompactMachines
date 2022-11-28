package dev.compactmods.machines.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.upgrade.RoomUpgradeAction;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.resources.ResourceKey;

public record UpgradeConnectionEntry<T extends RoomUpgradeAction>(String room, ResourceKey<RoomUpgradeAction> upgradeKey, T instance) {

    public static final Codec<UpgradeConnectionEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING
                    .fieldOf("room")
                    .forGetter(UpgradeConnectionEntry::room),

            ResourceKey.codec(RoomUpgradeAction.REG_KEY)
                    .fieldOf("upgrade")
                    .forGetter(UpgradeConnectionEntry::upgradeKey),

            MachineRoomUpgrades.REGISTRY.get().getCodec()
                    .fieldOf("data").forGetter(UpgradeConnectionEntry::instance)

    ).apply(i, UpgradeConnectionEntry::new));
}
