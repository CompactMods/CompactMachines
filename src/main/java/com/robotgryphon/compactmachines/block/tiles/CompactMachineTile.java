package com.robotgryphon.compactmachines.block.tiles;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.data.CompactMachineData;
import com.robotgryphon.compactmachines.data.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.data.MachineData;
import com.robotgryphon.compactmachines.reference.Reference;
import com.robotgryphon.compactmachines.util.CompactMachineUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CompactMachineTile extends TileEntity implements ICapabilityProvider, ITickable {
    public int machineId = -1;
    private boolean initialized = false;
    public boolean alreadyNotifiedOnTick = false;
    public long nextSpawnTick = 0;

    protected UUID owner;
    protected String schema;
    protected boolean locked = false;
    protected Set<String> playerWhiteList;

    public CompactMachineTile() {
        super(Registrations.MACHINE_TILE_ENTITY.get());

        playerWhiteList = new HashSet<>();
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        machineId = nbt.getInt("coords");
        // TODO customName = nbt.getString("CustomName");
        if (nbt.contains(Reference.CompactMachines.OWNER_NBT)) {
            owner = nbt.getUniqueId(Reference.CompactMachines.OWNER_NBT);
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
            list.forEach(nametag -> playerWhiteList.add(nametag.getString()));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt = super.write(nbt);

        nbt.putInt("coords", machineId);
        // nbt.putString("CustomName", customName.getString());

        if (owner != null) {
            nbt.putUniqueId(Reference.CompactMachines.OWNER_NBT, this.owner);
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

//    public void initStructure() {
//        if (this.coords != -1) {
//            return;
//        }
//
//        StructureTools.generateCubeForMachine(this);
//
//        double[] destination = new double[]{
//                this.coords * 1024 + 0.5 + this.getSize().getDimension() / 2,
//                42,
//                0.5 + this.getSize().getDimension() / 2
//        };
//
//        WorldSavedDataMachines.INSTANCE.addSpawnPoint(this.coords, destination);
//    }
//
//    public boolean isAllowedToEnter(EntityPlayer player) {
//        if (!isLocked()) {
//            return true;
//        }
//
//        if (!hasOwner()) {
//            return true;
//        }
//
//        if (player.getUniqueID().equals(owner)) {
//            return true;
//        }
//
//        if (isOnWhiteList(player)) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public boolean isOnWhiteList(EntityPlayer player) {
//        return playerWhiteList.contains(player.getName());
//    }
//
//    public boolean isOnWhiteList(String name) {
//        return playerWhiteList.contains(name);
//    }
//
//    public void addToWhiteList(EntityPlayer player) {
//        playerWhiteList.add(player.getName());
//    }
//
//    public void addToWhiteList(String playerName) {
//        playerWhiteList.add(playerName);
//    }
//
//    public void removeFromWhiteList(EntityPlayer player) {
//        playerWhiteList.remove(player.getName());
//    }
//
//    public void removeFromWhiteList(String playerName) {
//        playerWhiteList.remove(playerName);
//    }
//
//    public Set<String> getWhiteList() {
//        HashSet<String> result = new HashSet<>();
//        if (world.isRemote) {
//            Logz.warn("The TileEntityMachine#getWhiteList method should not be called on the client. Please report this to the mod author here: https://github.com/thraaawn/CompactMachines/issues/ Thanks!");
//            return result;
//        }
//
//        for (String name : playerWhiteList) {
//            result.add(name);
//        }
//
//        return result;
//    }
//
//    public boolean isLocked() {
//        return locked;
//    }
//
//    public void toggleLocked() {
//        this.locked = !this.locked;
//    }
//
//    public void setLocked(boolean state) {
//        this.locked = state;
//    }
//
//    public boolean hasNewSchema() {
//        return schema != null && schema.length() > 0;
//    }
//
//    public String getSchemaName() {
//        return schema;
//    }
//
//    public void setSchema(String schemaName) {
//        this.schema = schemaName;
//    }
//
//    public String getCustomName() {
//        return customName;
//    }



    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 1, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        BlockState state = null;
        if (this.world != null) {
            state = this.world.getBlockState(pos);
        }

        read(state, pkt.getNbtCompound());
    }

    @Override
    public void tick() {

    }

//    public void setCustomName(ITextComponent displayName) {
//        this.customName = displayName;
//    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setMachineId(int id) {
        this.machineId = id;
        this.markDirty();
    }

    public Optional<CompactMachineData> getMachineData() {
        if(this.machineId == 0)
            return Optional.empty();

        Optional<MachineData> machines = CompactMachineUtil.getMachineData(this.world);
        if(!machines.isPresent())
            return Optional.empty();

        return machines.get().getMachineData(this.machineId);
    }

    public boolean hasPlayersInside() {
        Optional<MachineData> machines = CompactMachineUtil.getMachineData(this.world);

        // No machine data present - probably none generated yet
        if(!machines.isPresent())
            return false;

        MachineData machineData = machines.get();
        Optional<CompactMachinePlayerData> playerData = machineData.getPlayerData(this.machineId);

        return playerData
                .map(CompactMachinePlayerData::hasPlayers)
                .orElse(false);
    }

    /*
     * Chunk-Loading triggers
     */

//    private void initialize() {
//        if (this.getWorld().isRemote) {
//            return;
//        }
//
//        if (!ChunkLoadingMachines.isMachineChunkLoaded(this.coords)) {
//            ChunkLoadingMachines.forceChunk(this.coords);
//        }
//
//    }

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

//    /*
//     * Capabilities
//     */
//    public RedstoneTunnelData getRedstoneTunnelForSide(EnumFacing side) {
//        if (!WorldSavedDataMachines.INSTANCE.redstoneTunnels.containsKey(this.coords)) {
//            return null;
//        }
//
//        return WorldSavedDataMachines.INSTANCE.redstoneTunnels.get(this.coords).get(side);
//    }
//
//    @Override
//    public int getConnectedDimensionId(EnumFacing side) {
//        return ConfigurationHandler.Settings.dimensionId;
//    }
//
//    @Override
//    public BlockPos getConnectedBlockPosition(EnumFacing side) {
//        if (WorldSavedDataMachines.INSTANCE == null || WorldSavedDataMachines.INSTANCE.tunnels == null) {
//            return null;
//        }
//
//        if (!WorldSavedDataMachines.INSTANCE.tunnels.containsKey(this.coords)) {
//            return null;
//        }
//
//        return WorldSavedDataMachines.INSTANCE.tunnels.get(this.coords).get(side);
//    }
//
//    public BlockPos getMachineWorldInsetPos(EnumFacing facing) {
//        BlockPos tunnelPos = this.getConnectedBlockPosition(facing);
//        if (tunnelPos == null) {
//            return null;
//        }
//
//        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
//        return tunnelPos.offset(insetDirection);
//    }
//
//    public IBlockState getConnectedBlockState(EnumFacing facing) {
//        BlockPos insetPos = getMachineWorldInsetPos(facing);
//        if (insetPos == null) {
//            return null;
//        }
//
//        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
//        return machineWorld.getBlockState(insetPos);
//    }
//
//    public TileEntity getConnectedTileEntity(EnumFacing facing) {
//        BlockPos insetPos = getMachineWorldInsetPos(facing);
//        if (insetPos == null) {
//            return null;
//        }
//
//        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
//        return machineWorld.getTileEntity(insetPos);
//    }
//
//    public boolean isInsideItself() {
//        if (this.getWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
//            return false;
//        }
//
//        return StructureTools.getCoordsForPos(this.getPos()) == this.coords;
//    }
//
//    public ItemStack getConnectedPickBlock(EnumFacing facing) {
//        BlockPos insetPos = getMachineWorldInsetPos(facing);
//        if (insetPos == null) {
//            return ItemStack.EMPTY;
//        }
//
//        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
//        IBlockState state = machineWorld.getBlockState(insetPos);
//        return state.getBlock().getItem(machineWorld, insetPos, state);
//    }
//
//    public int getRedstonePowerOutput(EnumFacing facing) {
//        if (this.coords == -1) {
//            return 0;
//        }
//
//        // We don't know the actual power on the client-side, which does not have the worldsaveddatamachines instance
//        if (WorldSavedDataMachines.INSTANCE == null || WorldSavedDataMachines.INSTANCE.redstoneTunnels == null) {
//            return 0;
//        }
//
//        HashMap<EnumFacing, RedstoneTunnelData> tunnelMapping = WorldSavedDataMachines.INSTANCE.redstoneTunnels.get(this.coords);
//        if (tunnelMapping == null) {
//            return 0;
//        }
//
//        RedstoneTunnelData tunnelData = tunnelMapping.get(facing);
//        if (tunnelData == null) {
//            return 0;
//        }
//
//
//        if (!tunnelData.isOutput) {
//            return 0;
//        }
//
//        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
//        if (!(machineWorld.getTileEntity(tunnelData.pos) instanceof TileEntityRedstoneTunnel)) {
//            return 0;
//        }
//
//        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelData.pos, this.getSize().getDimension());
//        BlockPos insetPos = tunnelData.pos.offset(insetDirection);
//        IBlockState insetBlockState = machineWorld.getBlockState(insetPos);
//
//        int power = 0;
//        if (insetBlockState.getBlock() instanceof BlockRedstoneWire) {
//            power = insetBlockState.getValue(BlockRedstoneWire.POWER);
//        } else {
//            power = machineWorld.getRedstonePower(insetPos, insetDirection);
//        }
//
//        return power;
//    }
//
//    @Override
//    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
//        if (isInsideItself()) {
//            return false;
//        }
//
//        if (world.isRemote || facing == null) {
//            if (CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
//                return true;
//            }
//
//            return super.hasCapability(capability, facing);
//        }
//
//        BlockPos tunnelPos = this.getConnectedBlockPosition(facing);
//        if (tunnelPos == null) {
//            return false;
//        }
//
//        World machineWorld = DimensionTools.getServerMachineWorld();
//        if (!(machineWorld.getTileEntity(tunnelPos) instanceof TileEntityTunnel)) {
//            return false;
//        }
//
//        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
//        BlockPos insetPos = tunnelPos.offset(insetDirection);
//
//        TileEntity te = machineWorld.getTileEntity(insetPos);
//        if (te != null && te instanceof ICapabilityProvider && te.hasCapability(capability, insetDirection.getOpposite())) {
//            return true;
//        }
//
//        if (CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
//            return true;
//        }
//
//        return false;
//    }
//
//    @Override
//    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
//        if (isInsideItself()) {
//            return null;
//        }
//
//        if (this.getWorld().isRemote || facing == null) {
//            if (CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
//                return CapabilityNullHandlerRegistry.getNullHandler(capability);
//            }
//
//            return super.getCapability(capability, facing);
//        }
//
//        BlockPos tunnelPos = this.getConnectedBlockPosition(facing);
//        if (tunnelPos == null) {
//            return null;
//        }
//
//        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
//        if (!(machineWorld.getTileEntity(tunnelPos) instanceof TileEntityTunnel)) {
//            return null;
//        }
//
//        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
//        BlockPos insetPos = tunnelPos.offset(insetDirection);
//
//        TileEntity te = machineWorld.getTileEntity(insetPos);
//        if (te instanceof ICapabilityProvider && te.hasCapability(capability, insetDirection.getOpposite())) {
//            return machineWorld.getTileEntity(insetPos).getCapability(capability, insetDirection.getOpposite());
//        }
//
//        if (CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
//            return CapabilityNullHandlerRegistry.getNullHandler(capability);
//        }
//
//        return null;
//    }
}
