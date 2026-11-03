package dev.compactmods.machines.upgrades.storage.tunnel;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A room's named storage definitions, keyed by name and kept in creation order. Persisted in its own
 * {@link #ATTACHMENT room attachment}; the storages' contents are not (see {@link NamedStorageService}).
 */
public record RoomNamedStorages(Map<String, RoomStorageDefinition> storages) {

    public static final RoomNamedStorages EMPTY = new RoomNamedStorages(Map.of());

    public static final MapCodec<RoomNamedStorages> MAP_CODEC = RoomStorageDefinition.CODEC.listOf()
            .<RoomNamedStorages>xmap(RoomNamedStorages::of, s -> List.copyOf(s.storages.values()))
            .optionalFieldOf("storages", EMPTY);

    public RoomNamedStorages {
        storages = Collections.unmodifiableMap(new LinkedHashMap<>(storages));
    }

    private static RoomNamedStorages of(List<RoomStorageDefinition> list) {
        final var map = new LinkedHashMap<String, RoomStorageDefinition>();
        list.forEach(def -> map.put(def.name(), def));
        return new RoomNamedStorages(map);
    }

    public static final Supplier<AttachmentType<RoomNamedStorages>> ATTACHMENT =
            Rooms.ATTACHMENT_TYPES.register("named_storages", () -> AttachmentType
                    .builder(() -> EMPTY)
                    .serialize(MAP_CODEC)
                    .build());

    /** Loads the class so {@link #ATTACHMENT} is registered. Call during mod construction. */
    public static void init() {
    }

    public static RoomNamedStorages get(RoomInstance room) {
        return room.getData(ATTACHMENT);
    }

    /** Applies {@code change} to the room's definitions, storing them only if they actually changed. */
    public static void update(RoomInstance room, UnaryOperator<RoomNamedStorages> change) {
        final var current = get(room);
        final var updated = change.apply(current);
        if (!updated.equals(current))
            room.setData(ATTACHMENT, updated);
    }

    public Optional<RoomStorageDefinition> get(String name) {
        return Optional.ofNullable(storages.get(name));
    }

    /** {@return the storage bound to the tunnel at {@code pos}}, if any */
    public Optional<RoomStorageDefinition> boundTo(BlockPos pos) {
        return storages.values().stream()
                .filter(def -> def.tunnel().map(pos::equals).orElse(false))
                .findFirst();
    }

    /** {@return these definitions with {@code def} stored under its name} */
    public RoomNamedStorages with(RoomStorageDefinition def) {
        final var map = new LinkedHashMap<>(storages);
        map.put(def.name(), def);
        return new RoomNamedStorages(map);
    }

    /** {@return these definitions with {@code def} renamed from {@code oldName}}, keeping its position in the order */
    public RoomNamedStorages renamed(String oldName, RoomStorageDefinition def) {
        final var map = new LinkedHashMap<String, RoomStorageDefinition>();
        storages.forEach((name, existing) -> {
            if (name.equals(oldName)) map.put(def.name(), def);
            else map.put(name, existing);
        });
        return new RoomNamedStorages(map);
    }
}
