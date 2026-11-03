package dev.compactmods.machines.upgrades.storage.tunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.room.Rooms;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Which machines outside a room expose its named storages, and on which faces. Kept as an index on the
 * room ({@link #ATTACHMENT}, persisted) because the bindings themselves live on the machines, which may
 * not be loaded when someone wants to know.
 *
 * <p>Whoever binds storages to machines keeps this up to date with {@link #set}; entries for machines
 * that went away are dropped by {@link #prune}, using the {@linkplain #setCheck registered check}.
 *
 * @param machines The storages each machine uses, by name, and the faces it exposes them on.
 */
public record RoomStorageLinks(Map<GlobalPos, Map<String, Set<Direction>>> machines) {

    /** A machine exposing a storage, on {@code faces}. */
    public record Link(GlobalPos machine, Set<Direction> faces) {
        public static final StreamCodec<ByteBuf, Link> STREAM_CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC, Link::machine,
                Direction.STREAM_CODEC.apply(ByteBufCodecs.list()).map(RoomStorageLinks::faces, List::copyOf), Link::faces,
                Link::new);

        public Link {
            faces = RoomStorageLinks.faces(faces);
        }
    }

    /** Whether a machine still exposes the room's storages (see {@link #setCheck}). */
    @FunctionalInterface
    public interface Check {
        boolean stillLinked(MinecraftServer server, RoomInstance room, GlobalPos machine);
    }

    public static final RoomStorageLinks EMPTY = new RoomStorageLinks(Map.of());

    private static final Codec<Set<Direction>> FACES_CODEC = Direction.CODEC.listOf()
            .xmap(RoomStorageLinks::faces, List::copyOf);

    private record Entry(GlobalPos machine, Map<String, Set<Direction>> uses) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("machine").forGetter(Entry::machine),
                Codec.unboundedMap(Codec.STRING, FACES_CODEC).fieldOf("uses").forGetter(Entry::uses)
        ).apply(i, Entry::new));
    }

    public static final MapCodec<RoomStorageLinks> MAP_CODEC = Entry.CODEC.listOf()
            .<RoomStorageLinks>xmap(
                    list -> {
                        final var map = new LinkedHashMap<GlobalPos, Map<String, Set<Direction>>>();
                        list.forEach(e -> map.put(e.machine(), e.uses()));
                        return new RoomStorageLinks(map);
                    },
                    links -> links.machines.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList())
            .optionalFieldOf("machines", EMPTY);

    public static final Supplier<AttachmentType<RoomStorageLinks>> ATTACHMENT =
            Rooms.ATTACHMENT_TYPES.register("storage_links", () -> AttachmentType
                    .builder(() -> EMPTY)
                    .serialize(MAP_CODEC)
                    .build());

    private static volatile Check check = (server, room, machine) -> true;

    public RoomStorageLinks {
        final var copy = new LinkedHashMap<GlobalPos, Map<String, Set<Direction>>>();
        machines.forEach((machine, uses) -> {
            final var usesCopy = new LinkedHashMap<String, Set<Direction>>();
            uses.forEach((name, faces) -> {
                if (!faces.isEmpty())
                    usesCopy.put(name, faces(faces));
            });
            if (!usesCopy.isEmpty())
                copy.put(machine, Map.copyOf(usesCopy));
        });
        machines = Collections.unmodifiableMap(copy);
    }

    /** Loads the class so {@link #ATTACHMENT} is registered. Call during mod construction. */
    public static void init() {
    }

    /** Sets how {@link #prune} tells whether a machine still exposes the room's storages. */
    public static void setCheck(Check check) {
        RoomStorageLinks.check = check;
    }

    public static RoomStorageLinks get(RoomInstance room) {
        return room.getData(ATTACHMENT);
    }

    /** Records which storages {@code machine} exposes, by name, and on which faces; empty forgets the machine. */
    public static void set(RoomInstance room, GlobalPos machine, Map<String, Set<Direction>> uses) {
        final var current = get(room);
        final var map = new LinkedHashMap<>(current.machines);
        map.put(machine, uses);
        final var updated = new RoomStorageLinks(map);
        if (!updated.equals(current))
            room.setData(ATTACHMENT, updated);
    }

    /** Forgets the machines that no longer pass the {@linkplain #setCheck check}, then returns what's left. */
    public static RoomStorageLinks prune(MinecraftServer server, RoomInstance room) {
        final var current = get(room);
        final var map = new LinkedHashMap<>(current.machines);
        map.keySet().removeIf(machine -> !check.stillLinked(server, room, machine));
        if (map.size() == current.machines.size())
            return current;

        final var updated = new RoomStorageLinks(map);
        room.setData(ATTACHMENT, updated);
        return updated;
    }

    /** {@return the machines exposing the storage named {@code name}}, in a stable order */
    public List<Link> linksTo(String name) {
        return machines.entrySet().stream()
                .filter(e -> e.getValue().containsKey(name))
                .map(e -> new Link(e.getKey(), e.getValue().get(name)))
                .sorted(Comparator.comparing((Link l) -> l.machine().dimension().identifier())
                        .thenComparing(l -> l.machine().pos()))
                .toList();
    }

    /** {@return {@code faces} as an immutable set in {@link Direction} order} */
    private static Set<Direction> faces(Collection<Direction> faces) {
        return faces.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(faces));
    }
}
