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
    private final HashMap<TunnelDefinition, HashSet<Direction>> capabilities;
    private final HashMap<TunnelDefinition, HashMap<Direction, LazyOptional<?>>> sidedCache;

    public RoomChunkCapabilities(LevelChunk chunk) {
        this.capabilities = new HashMap<>();
        this.sidedCache = new HashMap<>();
    }

    @Override
    public <CapType, TunnType extends TunnelDefinition> void addCapability(TunnType tunnel, Capability<CapType> capability, CapType instance, Direction side) {
        capabilities.putIfAbsent(tunnel, new HashSet<>());
        sidedCache.putIfAbsent(tunnel, new HashMap<>());

        var sides = capabilities.get(tunnel);
        if (!sides.isEmpty() && sides.contains(side))
            return;

        sides.add(side);
        var cache = sidedCache.get(tunnel);
        cache.put(side, LazyOptional.of(() -> instance));
    }

    @Override
    public <CapType, TunnType extends TunnelDefinition> void removeCapability(TunnType tunnel, Capability<CapType> capability, Direction side) {
        if (!capabilities.containsKey(tunnel))
            return;

        var sides = capabilities.get(tunnel);
        if (sides.contains(side)) {
            sides.remove(side);
            if (sidedCache.containsKey(tunnel)) {
                var cache = sidedCache.get(tunnel);
                if (cache.containsKey(side)) {
                    cache.get(side).invalidate();
                    cache.remove(side);
                }
            }
        }
    }

    @Override
    public <CapType, TunnType extends TunnelDefinition> LazyOptional<CapType> getCapability(TunnType tunnel, Capability<CapType> capability, Direction side) {
        if (side == null)
            return LazyOptional.empty();

        if (capabilities.containsKey(tunnel)) {
            // get sides tunnel applied to
            var c = capabilities.get(tunnel);
            if (c.contains(side) && sidedCache.containsKey(tunnel))
                return sidedCache.get(tunnel).get(side).cast();
        }

        return LazyOptional.empty();
    }
}
