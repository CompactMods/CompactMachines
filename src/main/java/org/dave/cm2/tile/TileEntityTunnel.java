package org.dave.cm2.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.dave.cm2.block.BlockTunnel;
import org.dave.cm2.world.tools.StructureTools;
import org.dave.cm2.utility.DimensionBlockPos;
import org.dave.cm2.world.tools.DimensionTools;
import org.dave.cm2.world.WorldSavedDataMachines;

public class TileEntityTunnel extends TileEntity implements ICapabilityProvider {

    public EnumFacing getMachineSide() {
        return this.getWorld().getBlockState(this.getPos()).getValue(BlockTunnel.MACHINE_SIDE);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(this.getWorld().isRemote) {
            return super.hasCapability(capability, facing);
        }

        DimensionBlockPos dimpos = WorldSavedDataMachines.INSTANCE.machinePositions.get(StructureTools.getCoordsForPos(this.getPos()));
        if(dimpos == null) {
            return false;
        }

        WorldServer realWorld = DimensionTools.getWorldServerForDimension(dimpos.getDimension());
        if(realWorld == null || !(realWorld.getTileEntity(dimpos.getBlockPos()) instanceof TileEntityMachine)) {
            return false;
        }

        EnumFacing machineSide = this.getMachineSide();
        BlockPos outsetPos = dimpos.getBlockPos().offset(machineSide);

        if(!(realWorld.getTileEntity(outsetPos) instanceof ICapabilityProvider)) {
            return false;
        }

        return realWorld.getTileEntity(outsetPos).hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(this.getWorld().isRemote) {
            return super.getCapability(capability, facing);
        }

        DimensionBlockPos dimpos = WorldSavedDataMachines.INSTANCE.machinePositions.get(StructureTools.getCoordsForPos(this.getPos()));
        if(dimpos == null) {
            return null;
        }

        WorldServer realWorld = DimensionTools.getWorldServerForDimension(dimpos.getDimension());
        if(realWorld == null || !(realWorld.getTileEntity(dimpos.getBlockPos()) instanceof TileEntityMachine)) {
            return null;
        }

        EnumFacing machineSide = this.getMachineSide();
        BlockPos outsetPos = dimpos.getBlockPos().offset(machineSide);

        if(!(realWorld.getTileEntity(outsetPos) instanceof ICapabilityProvider)) {
            return null;
        }

        return realWorld.getTileEntity(outsetPos).getCapability(capability, facing);
    }

}
