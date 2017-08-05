package org.dave.compactmachines3.misc;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.dave.compactmachines3.block.BlockMachine;
import org.dave.compactmachines3.block.BlockTunnel;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.TextFormattingHelper;

import java.util.List;

public class WailaProvider {
    public static void register(IWailaRegistrar registry) {
        MachineProvider mpInstance = new MachineProvider();
        TunnelProvider tpInstance = new TunnelProvider();

        registry.registerBodyProvider(mpInstance, BlockMachine.class);
        registry.registerBodyProvider(tpInstance, BlockTunnel.class);

        registry.registerStackProvider(mpInstance, BlockMachine.class);
        registry.registerStackProvider(tpInstance, BlockTunnel.class);
    }

    public static class TunnelProvider implements IWailaDataProvider {

        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return new ItemStack(accessor.getBlock(), 1, 0);
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return currenttip;
        }


        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            String translate = "enumfacing." + accessor.getBlockState().getValue(BlockTunnel.MACHINE_SIDE).getName();
            currenttip.add(TextFormatting.YELLOW + I18n.format(translate) + TextFormatting.RESET);
            return currenttip;
        }


        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
            return tag;
        }
    }

    public static class MachineProvider implements IWailaDataProvider {

        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return new ItemStack(accessor.getBlock(), 1, accessor.getMetadata());
        }


        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return currenttip;
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            if(!(accessor.getTileEntity() instanceof TileEntityMachine)) {
                return currenttip;
            }

            String nameOrId = "";
            TileEntityMachine te = (TileEntityMachine)accessor.getTileEntity();
            if(te.coords < 0 && te.getCustomName().length() == 0) {
                nameOrId = I18n.format("tooltip.compactmachines3.machine.coords.unused");
            } else if(te.getCustomName().length() > 0) {
                nameOrId = te.getCustomName();
            } else {
                nameOrId = "#" + te.coords;
            }
            currenttip.add(TextFormattingHelper.colorizeKeyValue(I18n.format("tooltip.compactmachines3.machine.coords", nameOrId)));

            String sideString = I18n.format("enumfacing.side", I18n.format("enumfacing." + accessor.getSide().getName()));
            currenttip.add(TextFormattingHelper.colorizeKeyValue(sideString));

            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
            return tag;
        }
    }
}
