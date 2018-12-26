package org.dave.compactmachines3.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.tile.TileEntityRedstoneTunnel;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.data.RedstoneTunnelData;
import org.dave.compactmachines3.world.tools.StructureTools;

import javax.annotation.Nullable;
import java.util.HashMap;

public class BlockRedstoneTunnel extends BlockBaseTunnel {
    public static final PropertyBool IS_OUTPUT = PropertyBool.create("output");

    public BlockRedstoneTunnel(Material material) {
        super(material);

        this.setDefaultState(blockState.getBaseState().withProperty(MACHINE_SIDE, EnumFacing.DOWN).withProperty(IS_OUTPUT, false));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(player.isSneaking()) {
            return false;
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        EnumFacing connectedSide = state.getValue(MACHINE_SIDE);

        double faceX = 0.0d;
        double faceY = 0.0d;
        if(facing.getAxis() == EnumFacing.Axis.X) {
            faceY = hitY;

            if(facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                faceX = hitZ;
            } else {
                faceX = 1.0d - hitZ;
            }
        } else if(facing.getAxis() == EnumFacing.Axis.Z) {
            faceY = hitY;

            if(facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                faceX = 1.0d - hitX;
            } else {
                faceX = hitX;
            }
        } else if(facing.getAxis() == EnumFacing.Axis.Y) {

            if(facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                faceY = hitZ;
                faceX = hitX;
            } else {
                faceY = 1.0d - hitZ;
                faceX = hitX;
            }
        }

        if(faceX >= 0.69d && faceX <= 0.94d && faceY >= 0.69d && faceY <= 0.94d) {
            world.setBlockState(pos, state.withProperty(IS_OUTPUT, !state.getValue(IS_OUTPUT)));
            WorldSavedDataMachines.INSTANCE.toggleRedstoneTunnelOutput(pos);

            notifyOverworldNeighbor(pos);
            world.notifyNeighborsOfStateChange(pos, Blockss.redstoneTunnel, false);
        } else {
            EnumFacing nextDirection = StructureTools.getNextDirection(connectedSide);

            int coords = StructureTools.getCoordsForPos(pos);
            HashMap<EnumFacing, RedstoneTunnelData> sideMapping = WorldSavedDataMachines.INSTANCE.redstoneTunnels.get(coords);
            while(sideMapping != null) {
                if (nextDirection == null) {
                    if(world.getTileEntity(pos) != null) {
                        world.removeTileEntity(pos);
                    }

                    IBlockState blockState = Blockss.wall.getDefaultState();
                    world.setBlockState(pos, blockState);

                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Itemss.redstoneTunnelTool));
                    WorldSavedDataMachines.INSTANCE.removeRedstoneTunnel(pos);
                    break;
                }

                if(sideMapping.get(nextDirection) == null) {
                    if(world.getTileEntity(pos) != null) {
                        world.removeTileEntity(pos);
                    }

                    world.setBlockState(pos, state.withProperty(MACHINE_SIDE, nextDirection));
                    WorldSavedDataMachines.INSTANCE.removeRedstoneTunnel(pos, connectedSide);
                    WorldSavedDataMachines.INSTANCE.addRedstoneTunnel(pos, nextDirection, state.getValue(IS_OUTPUT));
                    break;
                }

                nextDirection = StructureTools.getNextDirection(nextDirection);
            }

            notifyOverworldNeighbor(pos);
        }

        return true;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (!(blockAccess.getTileEntity(pos) instanceof TileEntityRedstoneTunnel)) {
            return 0;
        }

        TileEntityRedstoneTunnel redstoneTunnel = (TileEntityRedstoneTunnel) blockAccess.getTileEntity(pos);
        return redstoneTunnel.getRedstonePowerInput(blockState.getValue(MACHINE_SIDE));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityRedstoneTunnel();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if(meta >= 6) {
            state = state.withProperty(MACHINE_SIDE, EnumFacing.byIndex(meta-6));
            state = state.withProperty(IS_OUTPUT, true);
        } else {
            state = state.withProperty(MACHINE_SIDE, EnumFacing.byIndex(meta));
            state = state.withProperty(IS_OUTPUT, false);
        }

        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(MACHINE_SIDE).getIndex() + (state.getValue(IS_OUTPUT) ? 6 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MACHINE_SIDE, IS_OUTPUT);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        probeInfo.horizontal()
                .item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                .text(blockState.getValue(IS_OUTPUT) ? TextFormatting.DARK_GREEN + "Output" : TextFormatting.DARK_RED + "Input");

        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
    }
}
