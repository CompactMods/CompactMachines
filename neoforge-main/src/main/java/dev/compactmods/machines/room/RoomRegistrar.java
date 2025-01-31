package dev.compactmods.machines.room;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.data.CMRoomDataLocations;
import dev.compactmods.machines.data.CMDataFile;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.room.registration.IRoomRegistrar;
import dev.compactmods.machines.api.room.registration.IRoomBuilder;
import dev.compactmods.feather.MemoryGraph;
import dev.compactmods.machines.data.CodecHolder;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
import dev.compactmods.machines.util.MathUtil;
import dev.compactmods.spatial.aabb.AABBAligner;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RoomRegistrar implements IRoomRegistrar, CodecHolder<RoomRegistrar>, CMDataFile {

    public static final Logger LOGS = LogManager.getLogger();

    public static final Codec<RoomRegistrar> CODEC = RoomRegistrationNode.CODEC.listOf()
            .fieldOf("rooms")
            .xmap(RoomRegistrar::new, (RoomRegistrar x) -> List.copyOf(x.registrationNodes.values()))
            .codec();

    private final MemoryGraph graph;
    private final Map<String, RoomRegistrationNode> registrationNodes;
    private final Map<String, RoomInstance> instanceCache;
    private final MinecraftServer server;

    public RoomRegistrar(MinecraftServer server) {
        this.server = server;
        this.graph = new MemoryGraph();
        this.registrationNodes = new Object2ReferenceArrayMap<>();
        this.instanceCache = new Object2ObjectArrayMap<>();
    }

    private RoomRegistrar(List<RoomRegistrationNode> regNodes) {
        this(ServerLifecycleHooks.getCurrentServer());
        regNodes.forEach(this::registerDirty);
    }

    @Override
    public IRoomBuilder builder() {
        return new NewRoomBuilder();
    }

    @Override
    public MinecraftServer server() {
        return this.server;
    }

    @Override
    public AABB getNextBoundaries(RoomTemplate template) {
        final var region = MathUtil.getRegionPositionByIndex(registrationNodes.size());
        final var floor = MathUtil.getCenterWithY(region, 0);

        return AABBAligner.floor(template.getZeroBoundaries().move(floor), 0);
    }

    @Override
    public RoomInstance createNew(RoomTemplate template, UUID owner, Consumer<IRoomBuilder> override) {
        final var inst = IRoomRegistrar.super.createNew(template, owner, override);

        var node = new RoomRegistrationNode(UUID.randomUUID(), new RoomRegistrationNode.Data(inst));
        this.registrationNodes.put(inst.code(), node);
        this.graph.addNode(node);

        CompactMachines.roomApi().chunkManager().calculateChunks(inst.code(), node);

        instanceCache.put(inst.code(), inst);
        return inst;
    }

    @Override
    public boolean isRegistered(String room) {
        return registrationNodes.containsKey(room);
    }

    @Override
    public Optional<RoomInstance> get(String room) {
        final var regNode = registrationNodes.get(room);
        if (regNode == null)
            return Optional.empty();

        RoomInstance inst = getOrMakeRoomInstance(regNode);
        return Optional.of(inst);
    }

    @NotNull
    private RoomInstance getOrMakeRoomInstance(RoomRegistrationNode regNode) {
        try {
            if(instanceCache.containsKey(regNode.code()))
                return instanceCache.get(regNode.code());

            final var inst = new RoomInstance(server, CompactDimension.forServer(server), regNode.code(), regNode.defaultMachineColor(), regNode);
            instanceCache.put(regNode.code(), inst);
            return inst;
        } catch (MissingDimensionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count() {
        return registrationNodes.size();
    }

    @Override
    public Stream<String> allRoomCodes() {
        return registrationNodes.keySet().stream();
    }

    @Override
    public Stream<RoomInstance> allRooms() {
        return registrationNodes.values()
                .stream()
                .map(this::getOrMakeRoomInstance);
    }

    private void registerDirty(RoomRegistrationNode node) {
        registrationNodes.putIfAbsent(node.code(), node);
        graph.addNode(node);
    }

    @Override
    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.DATA_ROOT.apply(server);
    }

    @Override
    public Codec<RoomRegistrar> codec() {
        return CODEC;
    }
}
