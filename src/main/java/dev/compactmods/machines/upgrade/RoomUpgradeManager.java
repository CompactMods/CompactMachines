package dev.compactmods.machines.upgrade;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.mojang.serialization.Codec;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.room.graph.CompactMachineRoomNode;
import dev.compactmods.machines.upgrade.graph.RoomUpgradeConnection;
import dev.compactmods.machines.upgrade.graph.RoomUpgradeGraphNode;
import dev.compactmods.machines.upgrade.graph.UpgradeConnectionEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoomUpgradeManager extends SavedData {

    public static final String DATA_NAME = CompactMachines.MOD_ID + "_upgrades";

    private final HashMap<ResourceLocation, RoomUpgradeGraphNode> upgradeNodes;
    private final HashMap<ChunkPos, CompactMachineRoomNode> roomNodes;

    private final MutableValueGraph<IGraphNode, IGraphEdge> graph;

    private static final Codec<List<UpgradeConnectionEntry>> UPGRADE_CONNECTIONS_CODEC = UpgradeConnectionEntry.CODEC.listOf();

    private RoomUpgradeManager() {
        upgradeNodes = new HashMap<>();
        roomNodes = new HashMap<>();

        graph = ValueGraphBuilder
                .directed()
                .build();
    }

    @Nonnull
    public static RoomUpgradeManager get(ServerLevel level) {
        DimensionDataStorage sd = level.getDataStorage();
        return sd.computeIfAbsent(RoomUpgradeManager::fromNbt, RoomUpgradeManager::new, DATA_NAME);
    }

    private static RoomUpgradeManager fromNbt(CompoundTag tag) {
        final var inst = new RoomUpgradeManager();

        if (tag.contains("upgrades")) {
            final var upgrades = UPGRADE_CONNECTIONS_CODEC
                    .parse(NbtOps.INSTANCE, tag.getList("upgrades", Tag.TAG_COMPOUND))
                    .getOrThrow(true, CompactMachines.LOGGER::error);

            upgrades.forEach(conn -> {
                final var room = conn.room();
                final var data = conn.instance();
                if (!inst.addUpgrade(data, room))
                    CompactMachines.LOGGER.warn("Failed to load room upgrade for room {}: {}", room, conn.upgradeKey().toString());
            });
        }

        return inst;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (!roomNodes.isEmpty() || !upgradeNodes.isEmpty()) {
            final var upgReg = MachineRoomUpgrades.REGISTRY.get();

            List<UpgradeConnectionEntry> upgradeList = new ArrayList<>();
            for (var upg : upgradeNodes.values()) {
                final var id = upg.key();
                final var upgradeInst = upgReg.getValue(id);

                final var uKey = ResourceKey.create(RoomUpgrade.REG_KEY, id);

                this.roomsWith(uKey).forEach(room -> {
                    upgradeList.add(new UpgradeConnectionEntry(room, uKey, upgradeInst));
                });
            }

            if (!upgradeList.isEmpty()) {
                final var upgrades = UPGRADE_CONNECTIONS_CODEC.encodeStart(NbtOps.INSTANCE, upgradeList)
                        .getOrThrow(true, CompactMachines.LOGGER::error);

                tag.put("upgrades", upgrades);
            }

        }

        return tag;
    }

    public <T extends RoomUpgrade> boolean addUpgrade(T upgrade, ChunkPos room) {
        final var upgradeNode = upgradeNodes.computeIfAbsent(upgrade.getRegistryName(), rl -> {
            final var node = new RoomUpgradeGraphNode(rl);
            return graph.addNode(node) ? node : null;
        });

        final var roomNode = roomNodes.computeIfAbsent(room, p -> {
            final var nn = new CompactMachineRoomNode(p);
            return graph.addNode(nn) ? nn : null;
        });

        if (upgradeNode == null || roomNode == null)
            return false;

        graph.putEdgeValue(roomNode, upgradeNode, new RoomUpgradeConnection<>(upgrade));
        setDirty();

        return true;
    }

    public <T extends RoomUpgrade> boolean removeUpgrade(T upgrade, ChunkPos room) {
        if(!upgradeNodes.containsKey(upgrade.getRegistryName()))
            return true;

        if(!roomNodes.containsKey(room))
            return true;

        final var uNode = upgradeNodes.get(upgrade.getRegistryName());
        final var rNode = roomNodes.get(room);
        graph.removeEdge(rNode,  uNode);
        setDirty();
        return true;
    }

    public Stream<ChunkPos> roomsWith(ResourceKey<RoomUpgrade> upgradeKey) {
        if (!upgradeNodes.containsKey(upgradeKey.location()))
            return Stream.empty();

        return upgradeNodes.values().stream()
                .filter(upg -> upg.key().equals(upgradeKey.location()))
                .flatMap(upg -> graph.adjacentNodes(upg).stream())
                .filter(CompactMachineRoomNode.class::isInstance)
                .map(CompactMachineRoomNode.class::cast)
                .map(CompactMachineRoomNode::pos);
    }

    public <T extends RoomUpgrade> Stream<RoomUpgradeInstance<T>> implementing(Class<T> inter) {
        final var reg = MachineRoomUpgrades.REGISTRY.get();

        // Find all applicable upgrades in registry
        final var matchedUpgrades = reg.getValues().stream()
                .filter(inter::isInstance)
                .map(RoomUpgrade::getRegistryName)
                .collect(Collectors.toSet());

        // Filter graph upgrades, find matching root nodes
        final var matchedUpgradeNodes = upgradeNodes.values()
                .stream()
                .filter(upg -> matchedUpgrades.contains(upg.key()))
                .collect(Collectors.toSet());

        // Build a set of matched upgrade instance nodes

        // TODO - Figure out why this filter isn't working (in edgeValue - comparison?)
        HashSet<RoomUpgradeInstance<T>> instances = new HashSet<>();
        final var roomNodes = new HashSet<>();
        for (RoomUpgradeGraphNode upgNode : matchedUpgradeNodes) {
            for (IGraphNode adjNode : graph.adjacentNodes(upgNode)) {
                if (adjNode instanceof CompactMachineRoomNode roomNode) {
                    graph.edgeValue(roomNode, upgNode).ifPresent(edv -> {
                        if (edv instanceof RoomUpgradeConnection conn && inter.isInstance(conn.instance()))
                            instances.add(new RoomUpgradeInstance<>(inter.cast(conn.instance()), roomNode.pos()));
                    });
                }
            }
        }

        // Stream the instances off the set built above
        return instances.stream();
    }

    public boolean hasUpgrade(ChunkPos room, RoomUpgrade upgrade) {
        if(!upgradeNodes.containsKey(upgrade.getRegistryName()))
            return false;

        if(!roomNodes.containsKey(room))
            return false;

        final var upgNode = upgradeNodes.get(upgrade.getRegistryName());
        final var roomNode = roomNodes.get(room);

        return graph.hasEdgeConnecting(roomNode, upgNode);
    }
}
