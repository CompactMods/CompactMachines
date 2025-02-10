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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

class RoomRegistrarData implements CodecHolder<RoomRegistrarData>, CMDataFile {

    public static final Codec<RoomRegistrarData> CODEC = RoomRegistrationNode.CODEC.listOf()
            .fieldOf("rooms")
            .xmap(RoomRegistrarData::new, (RoomRegistrarData x) -> List.copyOf(x.registrationNodes.values()))
            .codec();

    private final MinecraftServer server;
    private final Map<String, RoomRegistrationNode> registrationNodes;

    public RoomRegistrarData(MinecraftServer server) {
        this.server = server;
        this.registrationNodes = new Object2ReferenceArrayMap<>();
    }

    private RoomRegistrarData(List<RoomRegistrationNode> regNodes) {
        this(ServerLifecycleHooks.getCurrentServer());
        regNodes.forEach(this::registerDirty);
    }

    public AABB getNextBoundaries(RoomTemplate template) {
        final var region = MathUtil.getRegionPositionByIndex(registrationNodes.size());
        final var floor = MathUtil.getCenterWithY(region, 0);

        return AABBAligner.floor(template.getZeroBoundaries().move(floor), 0);
    }

    public boolean isRegistered(String room) {
        return registrationNodes.containsKey(room);
    }

    public Optional<RoomRegistrationNode> get(String room) {
        final var regNode = registrationNodes.get(room);
        return Optional.ofNullable(regNode);
    }

    public void put(RoomRegistrationNode node) {
        this.registrationNodes.put(node.code(), node);
    }

    public long count() {
        return registrationNodes.size();
    }


    public Stream<String> allRoomCodes() {
        return registrationNodes.keySet().stream();
    }

    private void registerDirty(RoomRegistrationNode node) {
        registrationNodes.putIfAbsent(node.code(), node);
    }

    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.DATA_ROOT.apply(server);
    }

    @Override
    public Codec<RoomRegistrarData> codec() {
        return CODEC;
    }

    public Stream<RoomRegistrationNode> allRoomData() {
        return registrationNodes.values().stream();
    }
}
