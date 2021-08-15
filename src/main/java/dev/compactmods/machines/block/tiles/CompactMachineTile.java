package dev.compactmods.machines.block.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import dev.compactmods.machines.api.tunnels.ICapableTunnel;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import dev.compactmods.machines.reference.Reference;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import dev.compactmods.machines.tunnels.TunnelHelper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

public class CompactMachineTile extends TileEntity implements ICapabilityProvider, ITickableTileEntity {
    public int machineId = -1;
    private boolean initialized = false;
    public boolean alreadyNotifiedOnTick = false;
    public long nextSpawnTick = 0;

    protected UUID owner;
    protected String schema;
    protected boolean locked = false;
    protected Set<String> playerWhiteList;

    public CompactMachineTile() {
        super(Registration.MACHINE_TILE_ENTITY.get());

        playerWhiteList = new HashSet<>();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        if (ServerConfig.MACHINE_CHUNKLOADING.get())
            doChunkload(true);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if (ServerConfig.MACHINE_CHUNKLOADING.get())
            doChunkload(false);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (ServerConfig.MACHINE_CHUNKLOADING.get())
            doChunkload(false);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);

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

        playerWhiteList = new HashSet<>();
        if (nbt.contains("playerWhiteList")) {
            ListNBT list = nbt.getList("playerWhiteList", Constants.NBT.TAG_STRING);
            list.forEach(nametag -> playerWhiteList.add(nametag.getAsString()));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);

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

        if (playerWhiteList.size() > 0) {
            ListNBT list = new ListNBT();
            playerWhiteList.forEach(player -> {
                StringNBT nameTag = StringNBT.valueOf(player);
                list.add(nameTag);
            });

            nbt.put("playerWhiteList", list);
        }

        return nbt;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (level.isClientSide())
            return super.getCapability(cap, side);

        ServerWorld serverWorld = (ServerWorld) level;
        ServerWorld compactWorld = serverWorld.getServer().getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null)
            return LazyOptional.empty();

        Set<BlockPos> tunnelPositions = TunnelHelper.getTunnelsForMachineSide(this.machineId, serverWorld, side);
        if (tunnelPositions.isEmpty())
            return LazyOptional.empty();

        for (BlockPos possibleTunnel : tunnelPositions) {
            TunnelWallTile tile = (TunnelWallTile) compactWorld.getBlockEntity(possibleTunnel);
            if (tile == null)
                continue;

            Optional<TunnelDefinition> tunnel = tile.getTunnelDefinition();
            if (!tunnel.isPresent())
                continue;

            TunnelDefinition definition = tunnel.get();
            if (definition instanceof ICapableTunnel) {
                LazyOptional<T> capPoss = ((ICapableTunnel) definition).getInternalCapability(compactWorld, possibleTunnel, cap, side);
                if (capPoss.isPresent())
                    return capPoss;
            }
        }

        return LazyOptional.empty();
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 1, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT base = super.getUpdateTag();
        base.putInt("machine", this.machineId);

        if (level instanceof ServerWorld) {
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
        if (level instanceof ServerWorld) {
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
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);

        this.machineId = tag.getInt("machine");
        if (tag.contains("players")) {
            CompoundNBT players = tag.getCompound("players");
            // playerData = CompactMachinePlayerData.fromNBT(players);

        }

        if (tag.contains("owner"))
            owner = tag.getUUID("owner");
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        BlockState state = null;
        if (this.level != null) {
            state = this.level.getBlockState(worldPosition);
        }

        load(state, pkt.getTag());
    }

    @Override
    public void tick() {

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

    protected void doChunkload(boolean force) {
        if (level == null || level.isClientSide)
            return;

        getInternalChunkPos().ifPresent(chunk -> {
            ServerWorld compact = this.level.getServer().getLevel(Registration.COMPACT_DIMENSION);
            compact.setChunkForced(chunk.x, chunk.z, force);

        });
    }

    public void doPostPlaced() {
        if (this.level == null || this.level.isClientSide)
            return;

        MinecraftServer serv = this.level.getServer();
        if (serv == null)
            return;

        DimensionalPosition dp = new DimensionalPosition(
                this.level.dimension(),
                this.worldPosition
        );

        CompactMachineData extern = CompactMachineData.get(serv);
        extern.setMachineLocation(this.machineId, dp);

        doChunkload(true);
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
        if (level instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) level;
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
