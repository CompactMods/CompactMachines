package dev.compactmods.machines.block.tiles;

import java.util.Optional;
import java.util.UUID;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import dev.compactmods.machines.reference.Reference;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CompactMachineTile extends BlockEntity implements ICapabilityProvider {
    public int machineId = -1;
    private final boolean initialized = false;
    public boolean alreadyNotifiedOnTick = false;
    public long nextSpawnTick = 0;

    protected UUID owner;
    protected String schema;
    protected boolean locked = false;

    public CompactMachineTile(BlockPos pos, BlockState state) {
        super(Registration.MACHINE_TILE_ENTITY.get(), pos, state);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        machineId = nbt.getInt("coords");
        // TODO customName = nbt.getString("CustomName");
        if (nbt.contains(Reference.CompactMachines.OWNER_NBT)) {
            owner = nbt.getUUID(Reference.CompactMachines.OWNER_NBT);
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
        nbt.putInt("coords", machineId);
        // nbt.putString("CustomName", customName.getString());

        if (owner != null) {
            nbt.putUUID(Reference.CompactMachines.OWNER_NBT, this.owner);
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
//            Optional<CompactMachinePlayerData> playerData = Optional.empty();
//            try {
//                CompactMachinePlayerData psd = CompactMachinePlayerData.get(level.getServer());
//                // psd = psd.getPlayersInside(this.machineId);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            playerData.ifPresent(data -> {
//                CompoundNBT playerNbt = data.serializeNBT();
//                base.put("players", playerNbt);
//            });

            if (this.owner != null)
                base.putUUID("owner", this.owner);
        }

        return base;
    }

    public Optional<ChunkPos> getInternalChunkPos() {
        if (level instanceof ServerLevel) {
            MinecraftServer serv = level.getServer();
            if (serv == null)
                return Optional.empty();

            MachineConnections connections = MachineConnections.get(serv);
            if (connections == null)
                return Optional.empty();

            return connections.graph.getConnectedRoom(this.machineId);
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

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState state = null;
        if (this.level != null) {
            state = this.level.getBlockState(worldPosition);
        }

        super.onDataPacket(net, pkt);
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setMachineId(int id) {
        this.machineId = id;
        this.setChanged();
    }

    public boolean hasPlayersInside() {
        return false;
        // TODO
//        return CompactMachineCommonData
//                .getInstance()
//                .getPlayerData(machineId)
//                .map(CompactMachinePlayerData::hasPlayers)
//                .orElse(false);
    }

    public void doPostPlaced() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        MinecraftServer serv = this.level.getServer();
        if (serv == null)
            return;

        DimensionalPosition dp = new DimensionalPosition(
                this.level.dimension(),
                this.worldPosition
        );

        CompactMachineData extern = CompactMachineData.get(serv);
        extern.setMachineLocation(this.machineId, dp);

        this.setChanged();
    }

    public void handlePlayerLeft(UUID playerID) {
        // TODO
    }

    public void handlePlayerEntered(UUID playerID) {
        // TODO
    }

    public boolean mapped() {
        return getInternalChunkPos().isPresent();
    }

    public Optional<DimensionalPosition> getSpawn() {
        if (level instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) level;
            MinecraftServer serv = serverWorld.getServer();

            MachineConnections connections = MachineConnections.get(serv);
            if (connections == null)
                return Optional.empty();

            Optional<ChunkPos> connectedRoom = connections.graph.getConnectedRoom(machineId);

            if (!connectedRoom.isPresent())
                return Optional.empty();

            CompactRoomData roomData = CompactRoomData.get(serv);
            if (roomData == null)
                return Optional.empty();

            ChunkPos chunk = connectedRoom.get();
            return Optional.ofNullable(roomData.getSpawn(chunk));
        }

        return Optional.empty();
    }

//    @Override
//    public void update() {
//        if (!this.initialized && !this.isInvalid() && this.coords != -1) {
//            initialize();
//            this.initialized = true;
//        }
//
//        this.alreadyNotifiedOnTick = false;
//
//        if (nextSpawnTick == 0) {
//            nextSpawnTick = this.getWorld().getTotalWorldTime() + ConfigurationHandler.MachineSettings.spawnRate;
//        }
//
//        if (!this.getWorld().isRemote && this.coords != -1 && isInsideItself()) {
//            if (this.getWorld().getTotalWorldTime() % 20 == 0) {
//                world.playSound(null, getPos(),
//                        SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.wither.spawn")),
//                        SoundCategory.MASTER,
//                        1.0f,
//                        1.0f
//                );
//            }
//        }
//
//        if (!this.getWorld().isRemote && this.coords != -1 && this.getWorld().getTotalWorldTime() > nextSpawnTick) {
//            if (ConfigurationHandler.MachineSettings.allowPeacefulSpawns || ConfigurationHandler.MachineSettings.allowHostileSpawns) {
//                SpawnTools.spawnEntitiesInMachine(coords);
//            }
//
//            nextSpawnTick = this.getWorld().getTotalWorldTime() + ConfigurationHandler.MachineSettings.spawnRate;
//            this.markDirty();
//        }
//
//        /*
//        // Use this once we render in world or use the proxy world to determine client side capabilities.
//        if(!this.getWorld().isRemote && this.getWorld().getTotalWorldTime() % 20 == 0 && this.coords != -1) {
//            PackageHandler.instance.sendToAllAround(new MessageMachineChunk(this.coords), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32.0f));
//        }
//        */
//    }

//    @Override
//    public void onChunkUnload() {
//        super.onChunkUnload();
//        if (this.getWorld().isRemote) {
//            return;
//        }
//
//        if (ConfigurationHandler.Settings.forceLoadChunks) {
//            return;
//        }
//
//        ChunkLoadingMachines.unforceChunk(this.coords);
//    }
//
//    public boolean isInsideItself() {
//        if (this.getWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
//            return false;
//        }
//
//        return StructureTools.getCoordsForPos(this.getPos()) == this.coords;
//    }
}
