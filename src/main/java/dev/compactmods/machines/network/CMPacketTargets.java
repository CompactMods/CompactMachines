package dev.compactmods.machines.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

public class CMPacketTargets {

    public static final PacketDistributor<LevelChunk> TRACKING_ROOM = new PacketDistributor<>(
            CMPacketTargets::trackingRoom, NetworkDirection.PLAY_TO_CLIENT);

    private static Consumer<Packet<?>> trackingRoom(PacketDistributor<LevelChunk> dist, Supplier<LevelChunk> supplier) {
        LevelChunk roomChunk = supplier.get();
        Level level = roomChunk.getLevel();

        HashMap<UUID, ServerGamePacketListenerImpl> trackingPlayersGlobal = new HashMap<>();

        if (level instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) level;
            MinecraftServer server = serverWorld.getServer();

            MachineConnections connections = MachineConnections.get(server);
            CompactMachineData machines = CompactMachineData.get(server);

            Collection<Integer> linked = connections.graph.getMachinesFor(roomChunk.getPos());

            for (int machine : linked) {
                machines.getMachineLocation(machine).ifPresent(loc -> {
                    Optional<ServerLevel> machineWorld = loc.getWorld(server);
                    BlockPos machineWorldLocation = loc.getBlockPosition();
                    ChunkPos machineWorldChunk = new ChunkPos(machineWorldLocation);

                    machineWorld.ifPresent(mw -> {
                        mw.getChunkSource().chunkMap.getPlayers(machineWorldChunk, false).forEach(player -> {
                            if (!trackingPlayersGlobal.containsKey(player.getUUID()))
                                trackingPlayersGlobal.put(player.getUUID(), player.connection);
                        });
                    });
                });
            }
        }

        return pack -> trackingPlayersGlobal.values().forEach(conn -> conn.send(pack));
    }
}
