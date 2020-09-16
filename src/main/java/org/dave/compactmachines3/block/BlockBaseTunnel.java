package org.dave.compactmachines3.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
//import org.dave.compactmachines3.init.Blockss;
//import org.dave.compactmachines3.tile.BaseTileEntityTunnel;
//import org.dave.compactmachines3.tile.TileEntityMachine;
//import org.dave.compactmachines3.utility.DimensionBlockPos;
//import org.dave.compactmachines3.world.WorldSavedDataMachines;
//import org.dave.compactmachines3.world.tools.DimensionTools;
//import org.dave.compactmachines3.world.tools.StructureTools;

/**
 * Represents a block that has a tunnel attached to it.
 */
public abstract class BlockBaseTunnel extends BlockProtected implements IProbeInfoProvider {

    public static final DirectionProperty MACHINE_SIDE = DirectionProperty.create("machineside");

    public BlockBaseTunnel() {
        super(Block.Properties.create(Material.IRON));
        // this.op(1);
        // this.setLightLevel(1.0f);
    }

    // TODO: Client rendering
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

//    @Override
//    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
//        return CubeTools.shouldSideBeRendered(blockAccess, pos, side);
//    }


//    @Override
//    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
//        super.onNeighborChange(state, world, pos, neighbor);
//
//        if (world.isRemote()) {
//            return;
//        }
//
//        if (!(world.getTileEntity(pos) instanceof BaseTileEntityTunnel)) {
//            return;
//        }
//
//        BaseTileEntityTunnel tunnel = (BaseTileEntityTunnel) world.getTileEntity(pos);
//        if (tunnel.alreadyNotifiedOnTick) {
//            return;
//        }
//
//        tunnel.alreadyNotifiedOnTick = true;
//        notifyOverworldNeighbor(pos);
//    }

    // TODO: Dimension to overworld events
//    public void notifyOverworldNeighbor(BlockPos pos) {
//        DimensionBlockPos dimpos = WorldSavedDataMachines.INSTANCE.machinePositions.get(StructureTools.getCoordsForPos(pos));
//        if(dimpos == null) {
//            return;
//        }
//
//        ServerWorld realWorld = DimensionTools.getWorldServerForDimension(dimpos.getDimension());
//        if(realWorld == null || !(realWorld.getTileEntity(dimpos.getBlockPos()) instanceof TileEntityMachine)) {
//            return;
//        }
//
//        realWorld.notifyNeighborsOfStateChange(dimpos.getBlockPos(), Blockss.machine, false);
//    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());

//        TODO Tile Entity Impl
//        if (te instanceof BaseTileEntityTunnel) {
//            BaseTileEntityTunnel tnt = (BaseTileEntityTunnel) te;
//
//            String translate = "enumfacing." + blockState.get(BlockBaseTunnel.MACHINE_SIDE).name();
//            String format = TextFormatting.YELLOW + "{*" + translate + "*}" + TextFormatting.RESET;
//
//            probeInfo.horizontal()
//                    .item(new ItemStack(Items.COMPASS), probeInfo.defaultItemStyle().width(14).height(14))
//                    .text(CompoundText.create().text(format));
//
//            ItemStack connectedStack = tnt.getConnectedPickBlock();
//            if (connectedStack != null && !connectedStack.isEmpty()) {
//                probeInfo.horizontal().item(connectedStack, probeInfo.defaultItemStyle().width(14).height(14)).itemLabel(connectedStack);
//            }
//        }
    }
}
