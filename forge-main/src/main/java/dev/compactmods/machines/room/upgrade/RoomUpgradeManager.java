package dev.compactmods.machines.room.upgrade;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.mojang.serialization.Codec;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.room.graph.RoomReferenceNode;
import dev.compactmods.machines.upgrade.graph.RoomUpgradeConnection;
import dev.compactmods.machines.room.upgrade.graph.RoomUpgradeGraphNode;
import dev.compactmods.machines.upgrade.graph.UpgradeConnectionEntry;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoomUpgradeManager extends SavedData {

    public static final String DATA_NAME = Constants.MOD_ID + "_upgrades";

    private final HashMap<ResourceLocation, RoomUpgradeGraphNode> upgradeNodes;
    private final HashMap<String, RoomReferenceNode> roomNodes;
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

    public <T extends RoomUpgrade> boolean addUpgrade(T upgrade, String room) {
        final var upgRegistry = MachineRoomUpgrades.REGISTRY.get();

        final var upgradeNode = upgradeNodes.computeIfAbsent(upgRegistry.getKey(upgrade), rl -> {
            final var node = new RoomUpgradeGraphNode(rl);
            return graph.addNode(node) ? node : null;
        });

        final var roomNode = roomNodes.computeIfAbsent(room, p -> {
            final var nn = new RoomReferenceNode(p);
            return graph.addNode(nn) ? nn : null;
        });

        if (upgradeNode == null || roomNode == null)
            return false;

        graph.putEdgeValue(roomNode, upgradeNode, new RoomUpgradeConnection<>(upgrade));
        setDirty();

        return true;
    }

    public <T extends RoomUpgrade> boolean removeUpgrade(T upgrade, String room) {
        final var upgRegistry = MachineRoomUpgrades.REGISTRY.get();
        if(!upgRegistry.containsValue(upgrade)) return false;

        final var upgId = upgRegistry.getKey(upgrade);
        if(!upgradeNodes.containsKey(upgId))
            return true;

        if(!roomNodes.containsKey(room))
            return true;

        final var uNode = upgradeNodes.get(upgId);
        final var rNode = roomNodes.get(room);
        graph.removeEdge(rNode,  uNode);
        setDirty();
        return true;
    }

    public Stream<String> roomsWith(ResourceKey<RoomUpgrade> upgradeKey) {
        if (!upgradeNodes.containsKey(upgradeKey.location()))
            return Stream.empty();

        return upgradeNodes.values().stream()
                .filter(upg -> upg.key().equals(upgradeKey.location()))
                .flatMap(upg -> graph.adjacentNodes(upg).stream())
                .filter(RoomReferenceNode.class::isInstance)
                .map(RoomReferenceNode.class::cast)
                .map(RoomReferenceNode::code);
    }

    public <T extends RoomUpgrade> Stream<RoomUpgradeInstance<T>> implementing(Class<T> inter) {
        final var upgRegistry = MachineRoomUpgrades.REGISTRY.get();

        // Find all applicable upgrades in registry
        final var matchedUpgrades = upgRegistry.getValues().stream()
                .filter(inter::isInstance)
                .map(upgRegistry::getKey)
                .collect(Collectors.toSet());

        // Filter graph upgrades, find matching root nodes
        final var matchedUpgradeNodes = upgradeNodes.values()
                .stream()
                .filter(upg -> matchedUpgrades.contains(upg.key()))
                .collect(Collectors.toSet());

        // Build a set of matched upgrade instance nodes
        HashSet<RoomUpgradeInstance<T>> instances = new HashSet<>();
        final var roomNodes = new HashSet<>();
        for (RoomUpgradeGraphNode upgNode : matchedUpgradeNodes) {
            for (IGraphNode adjNode : graph.adjacentNodes(upgNode)) {
                if (adjNode instanceof RoomReferenceNode roomNode) {
                    graph.edgeValue(roomNode, upgNode).ifPresent(edv -> {
                        if (edv instanceof RoomUpgradeConnection conn && inter.isInstance(conn.instance()))
                            instances.add(new RoomUpgradeInstance<>(inter.cast(conn.instance()), roomNode.code()));
                    });
                }
            }
        }

        // Stream the instances off the set built above
        return instances.stream();
    }

    public boolean hasUpgrade(String room, RoomUpgrade upgrade) {
        final var upgRegistry = MachineRoomUpgrades.REGISTRY.get();
        if(!upgRegistry.containsValue(upgrade))
            return false;

        final var upgId = upgRegistry.getKey(upgrade);
        if(!upgradeNodes.containsKey(upgId))
            return false;

        if(!roomNodes.containsKey(room))
            return false;

        final var upgNode = upgradeNodes.get(upgId);
        final var roomNode = roomNodes.get(room);

        return graph.hasEdgeConnecting(roomNode, upgNode);
    }
}
