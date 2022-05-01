package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.IRoomInformation;
import dev.compactmods.machines.api.room.MachineRoomConnections;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.LevelBlockPosition;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CompactMachineBlockEntity extends BlockEntity implements ICapabilityProvider {
    public int machineId = -1;
    public long nextSpawnTick = 0;

    protected UUID owner;
    protected String schema;
    protected boolean locked = false;
    private ChunkPos roomChunk;
    private LazyOptional<IRoomInformation> room = LazyOptional.empty();

    public CompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.MACHINE_TILE_ENTITY.get(), pos, state);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.ROOM) return room.cast();

        if(level instanceof ServerLevel sl) {
            return getInternalChunkPos().map(roomId -> {
                try {
                    final var serv = sl.getServer();

                    final var tunnels = RoomTunnelData.get(serv, roomId);
                    final var graph = tunnels.getGraph();

                    final var supportingTunnels = graph.getTunnelsSupporting(machineId, side, cap);
                    final var firstSupported = supportingTunnels.findFirst();
                    if (firstSupported.isEmpty())
                        return super.getCapability(cap, side);

                    final var compact = serv.getLevel(Registration.COMPACT_DIMENSION);
                    if(compact == null)
                        throw new MissingDimensionException();

                    if(compact.getBlockEntity(firstSupported.get()) instanceof TunnelWallEntity tunnel) {
                        return tunnel.getTunnelCapability(cap, side);
                    } else {
                        return super.getCapability(cap, side);
                    }
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.fatal(e);
                    return super.getCapability(cap, side);
                }
            }).orElse(super.getCapability(cap, side));
        }

        return super.getCapability(cap, side);
    }

    @Nullable
    private IRoomInformation getRoom() {
        if (level instanceof ServerLevel sl) {
            return getInternalChunkPos().map(c -> {
                final var compact = sl.getServer().getLevel(Registration.COMPACT_DIMENSION);
                if(compact != null) {
                    var inChunk = compact.getChunk(c.x, c.z);
                    return inChunk.getCapability(Capabilities.ROOM).orElseThrow(RuntimeException::new);
                }

                return null;
            }).orElse(null);
        }

        return null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.room = LazyOptional.of(this::getRoom);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);

        machineId = nbt.getInt(MachineNbt.ID);
        // TODO customName = nbt.getString("CustomName");
        if (nbt.contains(MachineNbt.OWNER)) {
            owner = nbt.getUUID(MachineNbt.OWNER);
        } else {
            owner = null;
        }

        nextSpawnTick = nbt.getLong("spawntick");
        if (nbt.contains("schema")) {
            schema = nbt.getString("schema");
        } else {
            schema = null;
        }

        if (nbt.contains("locked")) {
            locked = nbt.getBoolean("locked");
        } else {
            locked = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt(MachineNbt.ID, machineId);
        // nbt.putString("CustomName", customName.getString());

        if (owner != null) {
            nbt.putUUID(MachineNbt.OWNER, this.owner);
        }

        nbt.putLong("spawntick", nextSpawnTick);
        if (schema != null) {
            nbt.putString("schema", schema);
        }

        nbt.putBoolean("locked", locked);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag base = super.getUpdateTag();
        base.putInt("machine", this.machineId);

        if (level instanceof ServerLevel) {
            // TODO - Internal player list
            if (this.owner != null)
                base.putUUID("owner", this.owner);
        }

        return base;
    }

    public Optional<ChunkPos> getInternalChunkPos() {
        if (level instanceof ServerLevel) {
            if(roomChunk != null)
                return Optional.of(roomChunk);

            MinecraftServer serv = level.getServer();
            if (serv == null)
                return Optional.empty();

            MachineRoomConnections connections;
            try {
                connections = MachineToRoomConnections.get(serv);
            } catch (MissingDimensionException e) {
                return Optional.empty();
            }

            var chunk = connections.getConnectedRoom(this.machineId);
            chunk.ifPresent(c -> this.roomChunk = c);
            return chunk;
        }

        return Optional.empty();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        this.machineId = tag.getInt("machine");
        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            // playerData = CompactMachinePlayerData.fromNBT(players);

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

    public void setMachineId(int id) {
        this.machineId = id;
        this.updateMapping();
    }

    public boolean hasPlayersInside() {
        // TODO
        return false;
    }

    public void doPostPlaced() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        MinecraftServer serv = this.level.getServer();
        if (serv == null)
            return;

        LevelBlockPosition dp = new LevelBlockPosition(
                this.level.dimension(),
                this.worldPosition
        );

        try {
            CompactMachineData extern = CompactMachineData.get(serv);
            extern.setMachineLocation(this.machineId, dp);

            this.setChanged();
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
        }
    }

    public boolean mapped() {
        return getInternalChunkPos().isPresent();
    }

    public Optional<LevelBlockPosition> getSpawn() {
        if (level instanceof ServerLevel serverWorld) {
            MinecraftServer serv = serverWorld.getServer();

            MachineRoomConnections connections = null;
            try {
                connections = MachineToRoomConnections.get(serv);
            } catch (MissingDimensionException e) {
                return Optional.empty();
            }

            Optional<ChunkPos> connectedRoom = connections.getConnectedRoom(machineId);

            if (connectedRoom.isEmpty())
                return Optional.empty();

            try {
                final var roomData = CompactRoomData.get(serv);

                ChunkPos chunk = connectedRoom.get();
                return Optional.ofNullable(roomData.getSpawn(chunk));
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.fatal(e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public void updateMapping() {
        this.room.invalidate();
        this.roomChunk = null;
        this.setChanged();
    }
}
