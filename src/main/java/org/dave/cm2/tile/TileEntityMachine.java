package org.dave.cm2.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.cm2.block.BlockMachine;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.world.ChunkLoadingMachines;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.DimensionTools;
import org.dave.cm2.world.tools.StructureTools;

import java.util.UUID;

public class TileEntityMachine extends TileEntity implements ICapabilityProvider, ITickable {
    public int coords = -1;
    private boolean initialized = false;
    public long lastNeighborUpdateTick = 0;

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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("coords", coords);
        compound.setString("CustomName", customName);

        if(hasOwner()) {
            compound.setUniqueId("owner", this.owner);
        }

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
    public BlockPos getTunnelForSide(EnumFacing side) {
        if(!WorldSavedDataMachines.INSTANCE.tunnels.containsKey(this.coords)) {
            return null;
        }

        return WorldSavedDataMachines.INSTANCE.tunnels.get(this.coords).get(side);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(this.getWorld().isRemote) {
            return super.hasCapability(capability, facing);
        }

        BlockPos tunnelPos = this.getTunnelForSide(facing);
        if(tunnelPos == null) {
            if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return true;
            }

            return false;
        }

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if(!(machineWorld.getTileEntity(tunnelPos) instanceof TileEntityTunnel)) {
            return false;
        }

        EnumFacing insetDirection = StructureTools.getInsetWallFacing(tunnelPos, this.getSize().getDimension());
        BlockPos insetPos = tunnelPos.offset(insetDirection);

        if(!(machineWorld.getTileEntity(insetPos) instanceof ICapabilityProvider)) {
            return false;
        }

        return machineWorld.getTileEntity(insetPos).hasCapability(capability, insetDirection.getOpposite());
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(this.getWorld().isRemote) {
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

        if(!(machineWorld.getTileEntity(insetPos) instanceof ICapabilityProvider)) {
            return null;
        }

        return machineWorld.getTileEntity(insetPos).getCapability(capability, insetDirection.getOpposite());
    }
}
