package org.dave.compactmachines3.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.item.ItemTunnelTool;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;
import org.dave.compactmachines3.misc.CubeTools;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.HashMap;

public class BlockWall extends BlockProtected {

    public BlockWall(Material material) {
        super(material);
        this.setLightOpacity(1);
        this.setLightLevel(1.0f);

        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return CubeTools.shouldSideBeRendered(blockAccess, pos, side);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack playerStack = player.getHeldItemMainhand();

        if(playerStack.isEmpty()) {
            return false;
        }

        if(!(playerStack.getItem() instanceof ItemTunnelTool)) {
            return false;
        }

        if(player.isSneaking()) {
            return false;
        }

        if(world.isRemote || !(player instanceof EntityPlayerMP)) {
            return true;
        }

        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return false;
        }

        EnumFacing tunnelSide = EnumFacing.DOWN;
        int coords = StructureTools.getCoordsForPos(pos);
        HashMap sideMapping = WorldSavedDataMachines.INSTANCE.tunnels.get(coords);
        while(sideMapping != null && tunnelSide != null) {
            if(sideMapping.get(tunnelSide) == null) {
                break;
            }

            tunnelSide = StructureTools.getNextDirection(tunnelSide);
        }

        if(tunnelSide != null) {
            IBlockState blockState = Blockss.tunnel.getDefaultState().withProperty(BlockTunnel.MACHINE_SIDE, tunnelSide);
            world.setBlockState(pos, blockState);

            playerStack.setCount(playerStack.getCount()-1);
            WorldSavedDataMachines.INSTANCE.addTunnel(pos, tunnelSide);
        } else {
            // TODO: Localization
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "All tunnels already used up"), false);
        }

        return true;
    }
}
