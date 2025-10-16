package dev.compactmods.machines.room;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.registration.IRoomBuilder;
import dev.compactmods.machines.api.room.registration.IRoomRegistrar;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.data.manager.CMSingletonDataFileManager;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CMRoomRegistrar implements IRoomRegistrar, AutoCloseable {

    private final MinecraftServer server;
    private final CMSingletonDataFileManager<RoomRegistrarData> ROOM_REGISTRAR_DATA;
    private final Map<String, RoomInstance> instanceCache;

    public CMRoomRegistrar(MinecraftServer server) {
        this.instanceCache = new Object2ObjectArrayMap<>();
        this.server = server;
        ROOM_REGISTRAR_DATA = new CMSingletonDataFileManager<>(server, "room_registrations", new RoomRegistrarData());
        ROOM_REGISTRAR_DATA.load();
    }

    @Override
    public AABB getNextBoundaries(RoomTemplate template) {
        return ROOM_REGISTRAR_DATA.data().getNextBoundaries(template);
    }

    @Override
    public IRoomBuilder builder() {
        return new NewRoomBuilder();
    }

    @Override
    public boolean isRegistered(String room) {
        return ROOM_REGISTRAR_DATA.data().isRegistered(room);
    }

    @Override
    public Optional<RoomInstance> get(String room) {
        return ROOM_REGISTRAR_DATA.data()
                .get(room)
                .map(this::getOrMakeRoomInstance);
    }

    @Override
    public long count() {
        return ROOM_REGISTRAR_DATA.data().count();
    }

    @Override
    public Stream<String> allRoomCodes() {
        if(ROOM_REGISTRAR_DATA == null) return Stream.empty();
        return ROOM_REGISTRAR_DATA.data().allRoomCodes();
    }

    @Override
    public Stream<RoomInstance> allRooms() {
        if(ROOM_REGISTRAR_DATA == null) return Stream.empty();
        return ROOM_REGISTRAR_DATA.data()
                .allRoomData()
                .map(this::getOrMakeRoomInstance);
    }

    @Override
    public void save() {
        if(ROOM_REGISTRAR_DATA == null) return;
        ROOM_REGISTRAR_DATA.save();
    }

    @Override
    public RoomInstance createNew(RoomTemplate template, UUID owner, Consumer<IRoomBuilder> override) {
        final Consumer<IRoomBuilder> preOverride = builder -> builder.defaultMachineColor(template.defaultMachineColor())
                .owner(owner)
                .boundaries(getNextBoundaries(template));

        // Make builder, set template defaults, then allow overrides
        final var b = new NewRoomBuilder();

        preOverride.andThen(override).accept(b);

        final var inst = b.build(server);

        var node = new RoomRegistrationNode(UUID.randomUUID(), new RoomRegistrationNode.Data(inst));

        ROOM_REGISTRAR_DATA.data().put(node);

        CompactMachines.chunkManager().calculateChunks(inst.code(), node);

        instanceCache.put(inst.code(), inst);
        return inst;
    }

    @NotNull
    private RoomInstance getOrMakeRoomInstance(RoomRegistrationNode regNode) {
        if (instanceCache.containsKey(regNode.code()))
            return instanceCache.get(regNode.code());

        final var inst = new RoomInstance(server, CompactDimension.LEVEL_KEY, regNode.code(), regNode.defaultMachineColor(), regNode);
        instanceCache.put(regNode.code(), inst);
        return inst;
    }

    @Override
    public void close() {
        save();
    }
}
