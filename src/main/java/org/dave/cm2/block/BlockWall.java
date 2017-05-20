package org.dave.cm2.block;

import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.item.ItemTunnelTool;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.StructureTools;

import javax.annotation.Nullable;
import java.util.HashMap;

public class BlockWall extends BlockProtected {

    public BlockWall(Material material) {
        super(material);
        this.setLightOpacity(1);
        this.setLightLevel(1.0f);

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack playerStack = player.getHeldItemMainhand();

        if(!ItemStackTools.isValid(playerStack) || ItemStackTools.isEmpty(playerStack)) {
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

            ItemStackTools.setStackSize(playerStack, ItemStackTools.getStackSize(playerStack)-1);
            WorldSavedDataMachines.INSTANCE.addTunnel(pos, tunnelSide);
        } else {
            // TODO: Localization
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "All tunnels already used up"));
        }

        return true;
    }
}
