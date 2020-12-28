package org.dave.compactmachines3.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.dave.compactmachines3.block.BlockTunnel;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.StructureTools;

public class BaseTileEntityTunnel extends TileEntity implements ITickable {
    public boolean alreadyNotifiedOnTick;

    public EnumFacing getMachineSide() {
        return this.getWorld().getBlockState(this.getPos()).getValue(BlockTunnel.MACHINE_SIDE);
    }

    public ItemStack getConnectedPickBlock() {
        DimensionBlockPos dimpos = WorldSavedDataMachines.getInstance().machinePositions.get(StructureTools.getIdForPos(this.getPos()));
        if(dimpos == null) {
            return ItemStack.EMPTY;
        }

        WorldServer realWorld = DimensionTools.getWorldServerForDimension(dimpos.getDimension());
        if(realWorld == null) {
            return ItemStack.EMPTY;
        }

        EnumFacing machineSide = this.getMachineSide();
        BlockPos outsetPos = dimpos.getBlockPos().offset(machineSide);

        IBlockState state = realWorld.getBlockState(outsetPos);
        return state.getBlock().getItem(world, outsetPos, state);
    }

    @Override
    public void update() {
        alreadyNotifiedOnTick = false;
    }
}
