package dev.compactmods.machines.api.room.upgrade;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public interface RoomUpgradeCodecs {
    @SuppressWarnings("unchecked")
    Codec<RoomUpgradeComponent> DISPATCH_CODEC = Codec.lazyInitialized(() -> {
        final var reg = BuiltInRegistries.REGISTRY
                .getOptional(CompactMachines.modRL("room_upgrades"))
                .map(r -> (Registry<RoomUpgradeComponentType<?>>) r);

        return (Codec<RoomUpgradeComponent>) reg
                .map(Registry::byNameCodec)
                .map(c -> c.dispatchStable(RoomUpgradeComponent::getType, RoomUpgradeComponentType::codec))
                .orElseThrow(() -> new RuntimeException("Room upgrade registry not registered yet; calling too early?"));
    });

    StreamCodec<RegistryFriendlyByteBuf, RoomUpgradeComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(DISPATCH_CODEC);
}
