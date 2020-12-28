package org.dave.compactmachines3.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.tile.TileEntityTunnel;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.Map;

public class BlockTunnel extends BlockBaseTunnel {

    public BlockTunnel(Material material) {
        super(material);

        this.setDefaultState(blockState.getBaseState().withProperty(MACHINE_SIDE, EnumFacing.DOWN));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityTunnel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(player.isSneaking()) {
            return false;
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        EnumFacing connectedSide = state.getValue(MACHINE_SIDE);
        EnumFacing nextDirection = StructureTools.getNextDirection(connectedSide);

        int id = StructureTools.getIdForPos(pos);
        Map<EnumFacing, BlockPos> sideMapping = WorldSavedDataMachines.getInstance().tunnels.get(id);
        if (sideMapping == null) {
            return true;
        }
        while (true) {
            if (nextDirection == null) {
                if(world.getTileEntity(pos) != null) {
                    world.removeTileEntity(pos);
                }

                IBlockState blockState = Blockss.wall.getDefaultState();
                world.setBlockState(pos, blockState);

                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Itemss.tunnelTool));
                WorldSavedDataMachines.getInstance().removeTunnel(pos);
                break;
            }

            if(sideMapping.get(nextDirection) == null) {
                if(world.getTileEntity(pos) != null) {
                    world.removeTileEntity(pos);
                }

                world.setBlockState(pos, state.withProperty(MACHINE_SIDE, nextDirection));
                WorldSavedDataMachines.getInstance().removeTunnel(pos, connectedSide);
                WorldSavedDataMachines.getInstance().addTunnel(pos, nextDirection);
                break;
            }

            nextDirection = StructureTools.getNextDirection(nextDirection);
        }

        notifyOverworldNeighbor(pos);
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(MACHINE_SIDE, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(MACHINE_SIDE).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MACHINE_SIDE);
    }
}
