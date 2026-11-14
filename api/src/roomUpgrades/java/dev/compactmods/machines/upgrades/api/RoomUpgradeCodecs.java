package dev.compactmods.machines.upgrades.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface RoomUpgradeCodecs {
    @SuppressWarnings("unchecked")
    Codec<RoomUpgradeComponent> DISPATCH_CODEC = Codec.lazyInitialized(() -> {
        final var reg = BuiltInRegistries.REGISTRY
                .getOptional(CompactMachinesCore.identifier("room_upgrade_component"))
                .map(r -> (Registry<RoomUpgradeComponentType<?>>) r);

        return (Codec<RoomUpgradeComponent>) reg
                .map(Registry::byNameCodec)
                // Components carry no config, so each type dispatches to a unit codec built from its
                // constructor; the serialized form is just { "type": "<id>" }.
                .map(c -> c.dispatchStable(RoomUpgradeComponent::getType, type -> MapCodec.unit(type.constructor())))
                .orElseThrow(() -> new RuntimeException("Room upgrade registry not registered yet; calling too early?"));
    });

    StreamCodec<RegistryFriendlyByteBuf, RoomUpgradeComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(DISPATCH_CODEC);
}
