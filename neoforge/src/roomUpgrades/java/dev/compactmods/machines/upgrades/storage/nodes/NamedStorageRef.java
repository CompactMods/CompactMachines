package dev.compactmods.machines.upgrades.storage.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.compactmods.machines.upgrades.storage.tunnel.NamedStorageService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

/**
 * A reference to one of a room's named storages — the value of the {@code compactmachines:named_storage}
 * graph type, picked from the canvas's choices on the named storage nodes.
 *
 * @param room Code of the room the storage belongs to.
 * @param name The storage's name.
 */
public record NamedStorageRef(String room, String name) {

    public static final Codec<NamedStorageRef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("room").forGetter(NamedStorageRef::room),
            Codec.STRING.fieldOf("name").forGetter(NamedStorageRef::name)
    ).apply(i, NamedStorageRef::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NamedStorageRef> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, NamedStorageRef::room,
            ByteBufCodecs.STRING_UTF8, NamedStorageRef::name,
            NamedStorageRef::new);

    /** {@return the room's named storages}, if the room still exists */
    public Optional<NamedStorageService> storages(MinecraftServer server) {
        final var rooms = RoomCapabilities.REGISTRY.getCapability(server);
        if (rooms == null)
            return Optional.empty();

        return rooms.get(room).map(this::storages);
    }

    private NamedStorageService storages(RoomInstance instance) {
        return instance.getCapability(StorageCapabilities.NAMED_STORAGES);
    }

    /** Shown in the canvas dropdown. */
    @Override
    public String toString() {
        return name;
    }
}
