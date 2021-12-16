package dev.compactmods.machines.rooms.capability;

import java.util.HashMap;
import java.util.HashSet;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.IMachineTunnels;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class RoomChunkData implements IMachineRoom {
    private final LevelChunk chunk;
    private final HashMap<TunnelDefinition, HashSet<Direction>> capabilities;
    private final HashMap<Capability<?>, HashMap<Direction, LazyOptional<?>>> sidedCache;
    private final MachineRoomTunnels tunnels;

    public RoomChunkData(LevelChunk chunk) {
        this.chunk = chunk;
        this.capabilities = new HashMap<>();
        this.sidedCache = new HashMap<>();
        this.tunnels = new MachineRoomTunnels(chunk);
    }

    @NotNull
    @Override
    public ChunkPos getChunk() {
        return chunk.getPos();
    }

    @NotNull
    @Override
    public ServerLevel getLevel() {
        return (ServerLevel) chunk.getLevel();
    }

    @NotNull
    @Override
    public IMachineTunnels getTunnels() {
        return this.tunnels;
    }

    @NotNull
    @Override
    public IRoomCapabilities getCapabilities() {
        return new IRoomCapabilities() {
            @Override
            public <CapType, TunnType extends TunnelDefinition> void addCapability(TunnType tunnel, Capability<CapType> capability, CapType instance, Direction side) {
                capabilities.putIfAbsent((TunnelDefinition) tunnel, new HashSet<>());
                sidedCache.putIfAbsent(capability, new HashMap<>());

                var sides = capabilities.get(capability);
                if (sides.contains(side))
                    return;

                sides.add(side);
                var cache = sidedCache.get(capability);
                cache.put(side, LazyOptional.of(() -> instance));
            }

            @Override
            public <CapType, TunnType extends TunnelDefinition> void removeCapability(TunnType tunnel, Capability<CapType> capability, Direction side) {
                if (!capabilities.containsKey(tunnel))
                    return;

                var sides = capabilities.get(tunnel);
                if (sides.contains(side)) {
                    sides.remove(side);
                    if (sidedCache.containsKey(capability)) {
                        var cache = sidedCache.get(capability);
                        if (cache.containsKey(side)) {
                            cache.get(side).invalidate();
                            cache.remove(side);
                        }
                    }
                }
            }

            @Override
            public <CapType, TunnType extends TunnelDefinition> LazyOptional<CapType> getCapability(Capability<CapType> capability, Direction side) {
                if (side == null)
                    return LazyOptional.empty();

                if (capabilities.containsKey(capability)) {
                    // get sides capability applied to
                    var c = capabilities.get(capability);
                    if (c.contains(side) && sidedCache.containsKey(capability))
                        return sidedCache.get(capability).get(side).cast();
                }

                return LazyOptional.empty();
            }
        };
    }
}
