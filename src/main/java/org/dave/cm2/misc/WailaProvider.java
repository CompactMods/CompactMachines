package org.dave.cm2.misc;

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
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.block.BlockMachine;
import org.dave.cm2.block.BlockTunnel;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.utility.Logz;

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

            TileEntityMachine te = (TileEntityMachine)accessor.getTileEntity();
            if(te.coords < 0) {
                currenttip.add(TextFormatting.YELLOW + I18n.format("tooltip." + CompactMachines2.MODID + ".machine.coords.unused") + TextFormatting.RESET);
            } else {
                currenttip.add(TextFormatting.DARK_GREEN + I18n.format("tooltip." + CompactMachines2.MODID + ".machine.coords", te.coords) + TextFormatting.RESET);
            }

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
