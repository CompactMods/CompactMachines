package org.dave.compactmachines3.tile;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.compactmachines3.block.BlockMachine;
import org.dave.compactmachines3.integration.CapabilityNullHandlerRegistry;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.world.ChunkLoadingMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.data.RedstoneTunnelData;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.SpawnTools;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.HashMap;
import java.util.UUID;

public class TileEntityMachine extends TileEntity implements ICapabilityProvider, ITickable {
    public int coords = -1;
    private boolean initialized = false;
    public long lastNeighborUpdateTick = 0;
    public long nextSpawnTick = 0;

    protected String customName = "";
    protected UUID owner;

    public TileEntityMachine() {
        super();
    }

    public EnumMachineSize getSize() {
        return this.getWorld().getBlockState(this.getPos()).getValue(BlockMachine.SIZE);
    }

    /*
     * NBT reading + writing
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        coords = compound.getInteger("coords");
        customName = compound.getString("CustomName");
        owner = compound.getUniqueId("owner");
        nextSpawnTick = compound.getLong("spawntick");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("coords", coords);
        compound.setString("CustomName", customName);

        if(hasOwner()) {
            compound.setUniqueId("owner", this.owner);
        }

        compound.setLong("spawntick", nextSpawnTick);

        return compound;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        // TODO: When the save game is transferred to another server/client, the profile of the owner might be null -> null pointer exception
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(getOwner()).getName();
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setOwner(EntityPlayer player) {
        if(player == null) {
            return;
        }

        setOwner(player.getUniqueID());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }


    /*
     * Chunk-Loading triggers
     */

    private void initialize() {
        if(this.getWorld().isRemote) {
            return;
        }

        if(!ChunkLoadingMachines.isMachineChunkLoaded(this.coords)) {
            ChunkLoadingMachines.forceChunk(this.coords);
        }

    }

    @Override
    public void update() {
        if(!this.initialized && !this.isInvalid() && this.coords != -1) {
            initialize();
            this.initialized = true;
        }

        if(nextSpawnTick == 0) {
            nextSpawnTick = this.getWorld().getTotalWorldTime() + ConfigurationHandler.MachineSettings.spawnRate;
        }

        if(!this.getWorld().isRemote && this.coords != -1 && isInsideItself()) {
            if (this.getWorld().getTotalWorldTime() % 20 == 0) {
                world.playSound(null, getPos(),
                        SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.wither.spawn")),
                        SoundCategory.MASTER,
                        1.0f,
                        1.0f
                );
            }
        }

        if(!this.getWorld().isRemote && this.coords != -1 && this.getWorld().getTotalWorldTime() > nextSpawnTick) {
            if(ConfigurationHandler.MachineSettings.allowPeacefulSpawns || ConfigurationHandler.MachineSettings.allowHostileSpawns) {
                SpawnTools.spawnEntitiesInMachine(coords);
            }

            nextSpawnTick = this.getWorld().getTotalWorldTime() + ConfigurationHandler.MachineSettings.spawnRate;
            this.markDirty();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if(this.getWorld().isRemote) {
            return;
        }

        if(ConfigurationHandler.Settings.forceLoadChunks) {
            return;
        }

        ChunkLoadingMachines.unforceChunk(this.coords);
    }

    /*
     * Capabilities
     */
    public RedstoneTunnelData getRedstoneTunnelForSide(EnumFacing side) {
        if(!WorldSavedDataMachines.INSTANCE.redstoneTunnels.containsKey(this.coords)) {
            return null;
        }

        return WorldSavedDataMachines.INSTANCE.redstoneTunnels.get(this.coords).get(side);
    }

    public BlockPos getTunnelForSide(EnumFacing side) {
        if(!WorldSavedDataMachines.INSTANCE.tunnels.containsKey(this.coords)) {
            return null;
        }

        return WorldSavedDataMachines.INSTANCE.tunnels.get(this.coords).get(side);
    }

    public BlockPos getMachineWorldInsetPos(EnumFacing facing) {
        BlockPos tunnelPos = this.getTunnelForSide(facing);
        if(tunnelPos == null) {
            return null;
        }

        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
        return tunnelPos.offset(insetDirection);
    }

    public IBlockState getConnectedBlockState(EnumFacing facing) {
        BlockPos insetPos = getMachineWorldInsetPos(facing);
        if(insetPos == null) {
            return null;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        return machineWorld.getBlockState(insetPos);
    }

    public TileEntity getConnectedTileEntity(EnumFacing facing) {
        BlockPos insetPos = getMachineWorldInsetPos(facing);
        if(insetPos == null) {
            return null;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        return machineWorld.getTileEntity(insetPos);
    }

    public boolean isInsideItself() {
        if(this.getWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return false;
        }

        return StructureTools.getCoordsForPos(this.getPos()) == this.coords;
    }

    public ItemStack getConnectedPickBlock(EnumFacing facing) {
        BlockPos insetPos = getMachineWorldInsetPos(facing);
        if(insetPos == null) {
            return ItemStack.EMPTY;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        IBlockState state = machineWorld.getBlockState(insetPos);
        return state.getBlock().getItem(machineWorld, insetPos, state);
    }

    public int getRedstonePowerOutput(EnumFacing facing) {
        if(this.coords == -1) {
            return 0;
        }

        // We don't know the actual power on the client-side, which does not have the worldsaveddatamachines instance
        if(WorldSavedDataMachines.INSTANCE == null || WorldSavedDataMachines.INSTANCE.redstoneTunnels == null) {
            return 0;
        }

        HashMap<EnumFacing, RedstoneTunnelData> tunnelMapping = WorldSavedDataMachines.INSTANCE.redstoneTunnels.get(this.coords);
        if(tunnelMapping == null) {
            return 0;
        }

        RedstoneTunnelData tunnelData = tunnelMapping.get(facing);
        if(tunnelData == null) {
            return 0;
        }


        if(!tunnelData.isOutput) {
            return 0;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if(!(machineWorld.getTileEntity(tunnelData.pos) instanceof TileEntityRedstoneTunnel)) {
            return 0;
        }

        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelData.pos, this.getSize().getDimension());
        BlockPos insetPos = tunnelData.pos.offset(insetDirection);
        IBlockState insetBlockState = machineWorld.getBlockState(insetPos);

        int power = 0;
        if(insetBlockState.getBlock() instanceof BlockRedstoneWire) {
            power = insetBlockState.getValue(BlockRedstoneWire.POWER);
        } else {
            power = machineWorld.getRedstonePower(insetPos, insetDirection);
        }

        return power;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(isInsideItself()) {
            return false;
        }

        if(this.getWorld().isRemote || facing == null) {
            if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
                return true;
            }

            return super.hasCapability(capability, facing);
        }

        BlockPos tunnelPos = this.getTunnelForSide(facing);
        if(tunnelPos == null) {
            return false;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if(!(machineWorld.getTileEntity(tunnelPos) instanceof TileEntityTunnel)) {
            return false;
        }

        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
        BlockPos insetPos = tunnelPos.offset(insetDirection);

        TileEntity te = machineWorld.getTileEntity(insetPos);
        if(te != null && te instanceof ICapabilityProvider && te.hasCapability(capability, insetDirection.getOpposite())) {
            return true;
        }

        if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
            return true;
        }

        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(isInsideItself()) {
            return null;
        }

        if(this.getWorld().isRemote || facing == null) {
            if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
                return CapabilityNullHandlerRegistry.getNullHandler(capability);
            }

            return super.getCapability(capability, facing);
        }

        BlockPos tunnelPos = this.getTunnelForSide(facing);
        if(tunnelPos == null) {
            return null;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if(!(machineWorld.getTileEntity(tunnelPos) instanceof TileEntityTunnel)) {
            return null;
        }

        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
        BlockPos insetPos = tunnelPos.offset(insetDirection);

        TileEntity te = machineWorld.getTileEntity(insetPos);
        if(te instanceof ICapabilityProvider && te.hasCapability(capability, insetDirection.getOpposite())) {
            return machineWorld.getTileEntity(insetPos).getCapability(capability, insetDirection.getOpposite());
        }

        if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
            return CapabilityNullHandlerRegistry.getNullHandler(capability);
        }

        return null;
    }
}
