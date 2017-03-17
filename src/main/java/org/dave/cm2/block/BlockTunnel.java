package org.dave.cm2.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Itemss;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.tile.TileEntityTunnel;
import org.dave.cm2.utility.DimensionBlockPos;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.DimensionTools;
import org.dave.cm2.world.tools.StructureTools;

import javax.annotation.Nullable;
import java.util.HashMap;

public class BlockTunnel extends BlockProtected implements ITileEntityProvider {
    public static final PropertyDirection MACHINE_SIDE = PropertyDirection.create("machineside");

    public BlockTunnel(Material material) {
        super(material);
        this.setLightOpacity(1);
        this.setLightLevel(1.0f);

        this.setDefaultState(blockState.getBaseState().withProperty(MACHINE_SIDE, EnumFacing.DOWN));
    }

    @Override
    public boolean isBlockProtected(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(player.isSneaking()) {
            return false;
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        EnumFacing connectedSide = state.getValue(MACHINE_SIDE);
        EnumFacing nextDirection = StructureTools.getNextDirection(connectedSide);

        int coords = StructureTools.getCoordsForPos(pos);
        HashMap sideMapping = WorldSavedDataMachines.INSTANCE.tunnels.get(coords);
        while(sideMapping != null) {
            if (nextDirection == null) {
                if(world.getTileEntity(pos) != null) {
                    world.removeTileEntity(pos);
                }

                IBlockState blockState = Blockss.wall.getDefaultState();
                world.setBlockState(pos, blockState);

                BlockPos inset = pos.offset(side);

                EntityItem entityItem = new EntityItem(world, inset.getX(), inset.getY(), inset.getZ(), new ItemStack(Itemss.tunnelTool, 1));
                entityItem.lifespan = 1200;
                entityItem.setPickupDelay(10);

                float f3 = 0.05F;
                entityItem.motionX = (float) world.rand.nextGaussian() * f3;
                entityItem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
                entityItem.motionZ = (float) world.rand.nextGaussian() * f3;

                world.spawnEntityInWorld(entityItem);

                WorldSavedDataMachines.INSTANCE.removeTunnel(pos);
                break;
            }

            if(sideMapping.get(nextDirection) == null) {
                if(world.getTileEntity(pos) != null) {
                    world.removeTileEntity(pos);
                }

                world.setBlockState(pos, state.withProperty(MACHINE_SIDE, nextDirection));
                WorldSavedDataMachines.INSTANCE.removeTunnel(pos, connectedSide);
                WorldSavedDataMachines.INSTANCE.addTunnel(pos, nextDirection);
                break;
            }

            nextDirection = StructureTools.getNextDirection(nextDirection);
        }

        notifyOverworldNeighbor(pos);
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        super.neighborChanged(state, world, pos, blockIn);

        if(world.isRemote) {
            return;
        }

        if(!(world.getTileEntity(pos) instanceof TileEntityTunnel)) {
            return;
        }

        notifyOverworldNeighbor(pos);
    }

    public void notifyOverworldNeighbor(BlockPos pos) {
        DimensionBlockPos dimpos = WorldSavedDataMachines.INSTANCE.machinePositions.get(StructureTools.getCoordsForPos(pos));
        if(dimpos == null) {
            return;
        }

        WorldServer realWorld = DimensionTools.getWorldServerForDimension(dimpos.getDimension());
        if(realWorld == null || !(realWorld.getTileEntity(dimpos.getBlockPos()) instanceof TileEntityMachine)) {
            return;
        }

        realWorld.notifyNeighborsOfStateChange(dimpos.getBlockPos(), Blockss.machine);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(MACHINE_SIDE, EnumFacing.getFront(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(MACHINE_SIDE).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MACHINE_SIDE);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityTunnel();
    }
}
