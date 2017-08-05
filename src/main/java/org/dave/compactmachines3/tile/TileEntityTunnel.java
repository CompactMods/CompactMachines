package org.dave.compactmachines3.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.dave.compactmachines3.block.BlockTunnel;
import org.dave.compactmachines3.integration.CapabilityNullHandlerRegistry;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.StructureTools;

public class TileEntityTunnel extends TileEntity implements ICapabilityProvider {

    public EnumFacing getMachineSide() {
        return this.getWorld().getBlockState(this.getPos()).getValue(BlockTunnel.MACHINE_SIDE);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(this.getWorld().isRemote || facing == null) {
            if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
                return true;
            }

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

        TileEntity te = realWorld.getTileEntity(outsetPos);
        if(te instanceof ICapabilityProvider && te.hasCapability(capability, machineSide.getOpposite())) {
            return true;
        }

        if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
            return true;
        }

        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(this.getWorld().isRemote) {
            if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
                return CapabilityNullHandlerRegistry.getNullHandler(capability);
            }

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

        TileEntity te = realWorld.getTileEntity(outsetPos);
        if(te instanceof ICapabilityProvider && te.hasCapability(capability, machineSide.getOpposite())) {
            return realWorld.getTileEntity(outsetPos).getCapability(capability, machineSide.getOpposite());
        }

        if(CapabilityNullHandlerRegistry.hasNullHandler(capability)) {
            return CapabilityNullHandlerRegistry.getNullHandler(capability);
        }

        return null;
    }

}
