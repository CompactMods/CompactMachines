package org.dave.cm2.tile;

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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.dave.cm2.block.BlockMachine;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.world.tools.StructureTools;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.world.ChunkLoadingMachines;
import org.dave.cm2.world.tools.DimensionTools;
import org.dave.cm2.world.WorldSavedDataMachines;

import javax.annotation.Nullable;

public class TileEntityMachine extends TileEntity implements ICapabilityProvider, ITickable {
    public int coords = -1;
    private boolean initialized = false;
    private static final int fluidPerOp = 10;
    private static final boolean randomlyNoCost = true;

    private MiniaturizationFluidTank tank;

    public TileEntityMachine() {
        super();

        tank = new MiniaturizationFluidTank();
    }

    public EnumMachineSize getSize() {
        return this.getWorld().getBlockState(this.getPos()).getValue(BlockMachine.SIZE);
    }

    public int getFluidLevel() {
        return tank.getFluidAmount();
    }

    public boolean hasEnoughEnergy(float multiplier) {
        return this.getFluidLevel() >= this.fluidPerOp * multiplier;
    }

    public boolean energyTick(float multiplier) {
        if(!hasEnoughEnergy(multiplier)) {
            Logz.info("Not enough energy");
            return false;
        }

        if(randomlyNoCost && Math.random() < 0.125) {
            return true;
        }

        int realCost = (int) (this.fluidPerOp * multiplier);
        FluidStack drainTest = tank.drainInternal(realCost, false);
        if(drainTest != null && drainTest.amount >= realCost) {
            tank.drainInternal(realCost, true);
            return true;
        }

        return false;
    }

    /*
     * NBT reading + writing
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        coords = compound.getInteger("coords");

        if(compound.hasKey("tank")) {
            NBTTagCompound tankCompound = (NBTTagCompound) compound.getTag("tank");
            tank.readFromNBT(tankCompound);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("coords", coords);
        tank.addTankTagCompound("tank", compound);

        return compound;
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
    private BlockPos getTunnelForSide(EnumFacing side) {
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

        // TODO: Add option to disable energy costs
        if(!this.hasEnoughEnergy(1.0f)) {
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
            if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return (T) this.tank;
            }
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

        this.energyTick(1.0f);

        // TODO: Add energy cost
        return machineWorld.getTileEntity(insetPos).getCapability(capability, insetDirection.getOpposite());
    }
}
