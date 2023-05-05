package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.machine.MachineEntityNbt;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.tunnel.TunnelWallEntity;
import dev.compactmods.machines.forge.tunnel.graph.traversal.ForgeTunnelTypeFilters;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelMachineFilters;
import dev.compactmods.machines.util.NbtUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "5.2.0")
public class LegacyCompactMachineBlockEntity extends BlockEntity {
    @Deprecated(forRemoval = true, since = "5.2.0")
    public static final String NBT_ROOM_POS = "room_pos";
    private static final String NBT_ROOM_CODE = MachineEntityNbt.NBT_ROOM_CODE;

    protected UUID owner;
    private String roomCode;

    public long nextSpawnTick = 0;
    protected String schema;
    protected boolean locked = false;
    private int legacyMachineId = -1;

    public LegacyCompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.LEGACY_MACHINE_ENTITY.get(), pos, state);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (level instanceof ServerLevel sl) {
            return getConnectedRoom().map(roomId -> {
                final var serv = sl.getServer();
                try {
                    final ServerLevel compactDim = CompactDimension.forServer(serv);
                    return TunnelConnectionGraph.forRoom(compactDim, roomId)
                            .tunnels(
                                    TunnelMachineFilters.sided(getLevelPosition(), side),
                                    ForgeTunnelTypeFilters.capability(cap)
                            )
                            .findFirst()
                            .map(tmi -> {
                                if (compactDim.getBlockEntity(tmi.location()) instanceof TunnelWallEntity tunnel) {
                                    return tunnel.getTunnelCapability(cap, side);
                                } else {
                                    return super.getCapability(cap, side);
                                }
                            }).orElse(super.getCapability(cap, side));
                } catch (MissingDimensionException e) {
                    return super.getCapability(cap, side);
                }
            }).orElse(super.getCapability(cap, side));
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.roomCode == null || this.legacyMachineId == -1)
            this.updateLegacyData();
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBT_ROOM_POS)) {
            final var originalRoomPos = NbtUtil.readChunkPos(nbt.get(NBT_ROOM_POS));
            CompactMachines.LOGGER.debug("Machine block has a chunk position specified, it will be rewritten to the new room code system." +
                    "The block at {} was originally connected to position {}", worldPosition, originalRoomPos);
        }

        if (nbt.contains(NBT_ROOM_CODE)) {
            this.roomCode = nbt.getString(NBT_ROOM_CODE);
        }

        if (nbt.contains(MachineNbt.OWNER)) {
            owner = nbt.getUUID(MachineNbt.OWNER);
        } else {
            owner = null;
        }

//        if (level != null && !level.isClientSide)
//            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        // nbt.putString("CustomName", customName.getString());

        if (owner != null) {
            nbt.putUUID(MachineNbt.OWNER, this.owner);
        }

        if (roomCode != null)
            nbt.putString(NBT_ROOM_CODE, roomCode);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        getConnectedRoom().ifPresent(room -> {
            data.putString(MachineEntityNbt.NBT_ROOM_CODE, roomCode);
        });

        if (level instanceof ServerLevel) {
            // TODO - Internal player list
            if (this.owner != null)
                data.putUUID(MachineNbt.OWNER, this.owner);
        }

        return data;
    }

    private void updateLegacyData() {
        if (level instanceof ServerLevel sl) {
            DimensionMachineGraph graph = DimensionMachineGraph.forDimension(sl);
            graph.connectedRoom(worldPosition).ifPresent(roomCode -> {
                CompactMachines.LOGGER.info("Rebinding machine {} ({}/{}) to room {}", legacyMachineId, worldPosition, level.dimension(), roomCode);
                this.roomCode = roomCode;
                this.legacyMachineId = -1;
                this.setChanged();
            });
        }
    }

    private Optional<String> getConnectedRoom() {
        if (level instanceof ServerLevel sl) {
            if (roomCode != null)
                return Optional.of(roomCode);

            final var graph = DimensionMachineGraph.forDimension(sl);

            var chunk = graph.connectedRoom(worldPosition);
            chunk.ifPresent(c -> this.roomCode = c);
            return chunk;
        }

        return Optional.ofNullable(roomCode);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            // playerData = CompactMachinePlayerData.fromNBT(players);
        }

        if (tag.contains(MachineEntityNbt.NBT_ROOM_CODE)) {
            this.roomCode = tag.getString(MachineEntityNbt.NBT_ROOM_CODE);
        }

        if (tag.contains("owner"))
            owner = tag.getUUID("owner");
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean hasPlayersInside() {
        // TODO
        return false;
    }

    public GlobalPos getLevelPosition() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    public void setConnectedRoom(String room) {
        if (level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.register(worldPosition, room);
            this.roomCode = room;
            setChanged();
        }
    }

    public void disconnect() {
        if (level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.unregisterMachine(worldPosition);
            this.roomCode = null;
            setChanged();
        }
    }
}