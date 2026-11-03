package dev.compactmods.machines.upgrades.storage.tunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

/**
 * The persisted half of a room-held named storage: everything except its contents, which live only
 * in memory (see {@link NamedStorageService}).
 *
 * @param name       Name of the storage, unique within its room across all tunnel types.
 * @param type       The tunnel type the storage was created with.
 * @param containers How many container items have been put in, which sets the capacity.
 * @param tunnel     Position of the tunnel wall bound to this storage, if any. Unbound storages keep
 *                   their data and can be bound again by name.
 */
public record RoomStorageDefinition(String name, ResourceKey<TunnelType> type, int containers, Optional<BlockPos> tunnel) {

    public static final Codec<RoomStorageDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(RoomStorageDefinition::name),
            ResourceKey.codec(TunnelType.REGISTRY_KEY).fieldOf("type").forGetter(RoomStorageDefinition::type),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("containers").forGetter(RoomStorageDefinition::containers),
            BlockPos.CODEC.optionalFieldOf("tunnel").forGetter(RoomStorageDefinition::tunnel)
    ).apply(inst, RoomStorageDefinition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RoomStorageDefinition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomStorageDefinition::name,
            ResourceKey.streamCodec(TunnelType.REGISTRY_KEY), RoomStorageDefinition::type,
            ByteBufCodecs.VAR_INT, RoomStorageDefinition::containers,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional), RoomStorageDefinition::tunnel,
            RoomStorageDefinition::new);

    public boolean isBound() {
        return tunnel.isPresent();
    }

    public RoomStorageDefinition withName(String name) {
        return new RoomStorageDefinition(name, type, containers, tunnel);
    }

    public RoomStorageDefinition withContainers(int containers) {
        return new RoomStorageDefinition(name, type, containers, tunnel);
    }

    public RoomStorageDefinition withTunnel(Optional<BlockPos> tunnel) {
        return new RoomStorageDefinition(name, type, containers, tunnel);
    }
}
