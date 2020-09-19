package org.dave.compactmachines3.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockWall extends BlockProtected {

    public BlockWall(Block.Properties props) {
        super(props);
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return pos.getY() == 40;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack held = player.getHeldItemMainhand();

        if(held.isEmpty())
            return ActionResultType.PASS;

        if(player.isSneaking())
            return ActionResultType.PASS;

        // TODO: Tunnels and Personal Shrinking Device

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

//    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
//        ItemStack playerStack = player.getHeldItemMainhand();
//
//        if(playerStack.isEmpty()) {
//            return false;
//        }
//
//        if(player.isSneaking()) {
//            return false;
//        }
//
//        if(ShrinkingDeviceUtils.isShrinkingDevice(playerStack)) {
//            return false;
//        }
//
//        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
//            return playerStack.getItem() instanceof ItemTunnelTool;
//        }
//
//        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
//            return false;
//        }
//
//        if(playerStack.getItem() instanceof ItemTunnelTool) {
//            EnumFacing tunnelSide = EnumFacing.DOWN;
//            int coords = StructureTools.getCoordsForPos(pos);
//            HashMap sideMapping = WorldSavedDataMachines.INSTANCE.tunnels.get(coords);
//            while(sideMapping != null && tunnelSide != null) {
//                if(sideMapping.get(tunnelSide) == null) {
//                    break;
//                }
//
//                tunnelSide = StructureTools.getNextDirection(tunnelSide);
//            }
//
//            if(tunnelSide != null) {
//                IBlockState blockState = Blockss.tunnel.getDefaultState().withProperty(BlockTunnel.MACHINE_SIDE, tunnelSide);
//                world.setBlockState(pos, blockState);
//
//                playerStack.setCount(playerStack.getCount()-1);
//                WorldSavedDataMachines.INSTANCE.addTunnel(pos, tunnelSide);
//            } else {
//                // TODO: Localization
//                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "All tunnels already used up"), false);
//            }
//
//            return true;
//        }
//
//        if(playerStack.getItem() instanceof ItemRedstoneTunnelTool) {
//            EnumFacing tunnelSide = EnumFacing.DOWN;
//            int coords = StructureTools.getCoordsForPos(pos);
//            HashMap<EnumFacing, RedstoneTunnelData> sideMapping = WorldSavedDataMachines.INSTANCE.redstoneTunnels.get(coords);
//            while(sideMapping != null && tunnelSide != null) {
//                if(sideMapping.get(tunnelSide) == null) {
//                    break;
//                }
//
//                tunnelSide = StructureTools.getNextDirection(tunnelSide);
//            }
//
//            if(tunnelSide != null) {
//                IBlockState blockState = Blockss.redstoneTunnel.getDefaultState().withProperty(BlockRedstoneTunnel.MACHINE_SIDE, tunnelSide);
//                world.setBlockState(pos, blockState);
//
//                playerStack.setCount(playerStack.getCount()-1);
//                WorldSavedDataMachines.INSTANCE.addRedstoneTunnel(pos, tunnelSide, false);
//            } else {
//                // TODO: Localization
//                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "All tunnels already used up"), false);
//            }
//
//            return true;
//        }
//
//        return false;
//    }
}
