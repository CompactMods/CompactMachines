package dev.compactmods.machines.rooms.capability;

import java.util.HashMap;
import java.util.HashSet;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class RoomChunkCapabilities implements IRoomCapabilities {
    private final HashMap<TunnelDefinition, HashSet<Direction>> placedTunnels;
    // private final HashMap<TunnelDefinition, HashMap<T, TunnelSideCache<T>>> sidedCache;

    public RoomChunkCapabilities(LevelChunk chunk) {
        this.placedTunnels = new HashMap<>();
        // this.sidedCache = new HashMap<>();
    }

    @Override
    public <CapType, TunnType extends TunnelDefinition> void addCapability(TunnType tunnel, Capability<CapType> capability, CapType instance, Direction side) {
        placedTunnels.putIfAbsent(tunnel, new HashSet<>());

        var sides = placedTunnels.get(tunnel);
        if (!sides.isEmpty() && sides.contains(side))
            return;

        sides.add(side);
    }

    @Override
    public <CapType, TunnType extends TunnelDefinition> void removeCapability(TunnType tunnel, Capability<CapType> capability, Direction side) {
        if (!placedTunnels.containsKey(tunnel))
            return;

        var sides = placedTunnels.get(tunnel);
        sides.remove(side);
    }

    @Override
    public <CapType, TunnType extends TunnelDefinition> LazyOptional<CapType> getCapability(TunnType tunnel, Capability<CapType> capability, Direction side) {
        if (side == null)
            return LazyOptional.empty();

        if (placedTunnels.containsKey(tunnel) && placedTunnels.get(tunnel).contains(side)) {
            // TODO: delegate capability lookup back to tunnel definition, with storage
        }

        return LazyOptional.empty();
    }
}
