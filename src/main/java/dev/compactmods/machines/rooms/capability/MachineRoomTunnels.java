package dev.compactmods.machines.rooms.capability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.IRoomTunnels;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.data.codec.NbtListCollector;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.INBTSerializable;

public class MachineRoomTunnels implements IRoomTunnels, INBTSerializable<CompoundTag> {
    private final LevelChunk chunk;
    private final HashMap<TunnelDefinition, Set<BlockPos>> typedPositions;

    public MachineRoomTunnels(LevelChunk chunk) {
        this.chunk = chunk;
        this.typedPositions = new HashMap<>();
    }

    /**
     * Registers a new tunnel applied to a position inside a machine room.
     *
     * @param type The type of tunnel being registered.
     * @param at   The position of the tunnel being registered.
     * @return True if successfully registered, false otherwise.
     */
    @Override
    public <T extends TunnelDefinition> boolean register(T type, BlockPos at) {
        // Reverse position map
        typedPositions.putIfAbsent(type, new HashSet<>(6));
        typedPositions.get(type).add(at);

        return true;
    }

    @Override
    public boolean unregister(BlockPos at) {
        var it = typedPositions.keySet().iterator();
        while (it.hasNext()) {
            var positions = typedPositions.get(it.next());

            // Clean up reverse position map
            if (positions.contains(at)) {
                positions.remove(at);
                if (positions.isEmpty())
                    it.remove();
            }
        }

        return true;
    }

    /**
     * Creates a stream of registered tunnels, given a connected machine position.
     * May contain multiple tunnels, as tunnels can handle individual tasks
     * that are combined in one block (ie Thermal Signalum-plated, EnderIO conduits)
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    @Override
    public Stream<ITunnelConnection> stream() {
        var combine = Stream.<BlockPos>of();
        for (var type : typedPositions.values())
            combine = Stream.concat(combine, type.stream());

        return combine.map(this::locatedAt)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @Override
    public Optional<ITunnelConnection> locatedAt(BlockPos pos) {
        if (chunk.getBlockState(pos).hasBlockEntity()) {
            if (chunk.getBlockEntity(pos) instanceof TunnelWallEntity t) {
                return Optional.of(t.getConnection());
            }
        }

        return Optional.empty();
    }

    @Override
    public Stream<BlockPos> stream(TunnelDefinition type) {
        if (!typedPositions.containsKey(type))
            return Stream.empty();

        return typedPositions.get(type).stream();
    }

    public CompoundTag serializeNBT() {
        var list = new ListTag();
        for (var type : typedPositions.keySet()) {
            var positions = typedPositions.get(type).stream()
                    .map(BlockPos::asLong)
                    .map(LongTag::valueOf)
                    .collect(NbtListCollector.toNbtList());

            CompoundTag typeNode = new CompoundTag();
            typeNode.putString("type", type.getRegistryName().toString());
            typeNode.put("positions", positions);
            list.add(typeNode);
        }

        CompoundTag tag = new CompoundTag();
        tag.put("list", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (!nbt.contains("list"))
            return;

        var list = nbt.getList("list", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            CompoundTag ctag = (CompoundTag) tag;
            ResourceLocation typeId = ResourceLocation.tryParse(ctag.getString("type"));
            if (!Tunnels.isRegistered(typeId)) {
                CompactMachines.LOGGER.warn("Unable to load in typed position info for type {}; type not registered.", typeId);
                continue;
            }

            var def = Tunnels.getDefinition(typeId);
            typedPositions.putIfAbsent(def, new HashSet<>(6));
            var positions = typedPositions.get(def);

            var blocks = ctag.getList("positions", Tag.TAG_LONG).stream()
                    .map(t -> (LongTag) t)
                    .map(LongTag::getAsLong)
                    .map(BlockPos::of)
                    .collect(Collectors.toSet());

            positions.addAll(blocks);
        }
    }
}
