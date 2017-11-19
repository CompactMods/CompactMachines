package org.dave.compactmachines3.compat;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import org.dave.compactmachines3.block.BlockFieldProjector;
import org.dave.compactmachines3.block.BlockMachine;
import org.dave.compactmachines3.block.BlockRedstoneTunnel;
import org.dave.compactmachines3.block.BlockTunnel;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.utility.TextFormattingHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class WailaProvider {
    public static void register(IWailaRegistrar registry) {
        Logz.info("Enabled support for Waila/Hwyla");
        MachineProvider mpInstance = new MachineProvider();
        TunnelProvider tpInstance = new TunnelProvider();
        RedstoneTunnelProvider rtpInstance = new RedstoneTunnelProvider();
        FieldProvider fpInstance = new FieldProvider();

        registry.registerBodyProvider(mpInstance, BlockMachine.class);
        registry.registerBodyProvider(tpInstance, BlockTunnel.class);
        registry.registerBodyProvider(rtpInstance, BlockRedstoneTunnel.class);
        registry.registerBodyProvider(fpInstance, BlockFieldProjector.class);

        registry.registerStackProvider(mpInstance, BlockMachine.class);
        registry.registerStackProvider(tpInstance, BlockTunnel.class);
        registry.registerStackProvider(rtpInstance, BlockRedstoneTunnel.class);
        registry.registerStackProvider(fpInstance, BlockFieldProjector.class);
    }

    public static class FieldProvider implements IWailaDataProvider {
        @Nonnull
        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            TileEntity te = accessor.getTileEntity();
            if(te instanceof TileEntityFieldProjector) {
                TileEntityFieldProjector tfp = (TileEntityFieldProjector) te;
                TileEntityFieldProjector master = tfp.getMasterProjector();

                if(master == null) {
                    return currenttip;
                }

                ItemStack crafting = master.getActiveCraftingResult();
                if(!crafting.isEmpty()) {
                    currenttip.add(TextFormatting.YELLOW + I18n.format("top.compactmachines3.currently_crafting") + TextFormatting.RESET + " " + crafting.getDisplayName());
                    return currenttip;
                }

                MultiblockRecipe result = MultiblockRecipes.tryCrafting(accessor.getWorld(), accessor.getPosition(), null);
                if(result != null) {
                    currenttip.add(TextFormatting.GREEN + I18n.format("top.compactmachines3.found_recipe_for") + TextFormatting.RESET + " " + result.getTargetStack().getDisplayName());
                }
            }

            return currenttip;
        }
    }

    public static class TunnelProvider implements IWailaDataProvider {
        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            String translate = "enumfacing." + accessor.getBlockState().getValue(BlockTunnel.MACHINE_SIDE).getName();
            currenttip.add(TextFormatting.YELLOW + I18n.format(translate) + TextFormatting.RESET);
            return currenttip;
        }
    }

    public static class RedstoneTunnelProvider implements IWailaDataProvider {
        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            String translate = "enumfacing." + accessor.getBlockState().getValue(BlockRedstoneTunnel.MACHINE_SIDE).getName();
            currenttip.add(TextFormatting.YELLOW + I18n.format(translate) + TextFormatting.RESET);
            return currenttip;
        }
    }


    public static class MachineProvider implements IWailaDataProvider {

        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
            return new ItemStack(accessor.getBlock(), 1, accessor.getMetadata());
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
            currenttip.add(TextFormattingHelper.colorizeKeyValue(I18n.format("tooltip.compactmachines3.machine.coords") + " " + nameOrId));

            String sideString = I18n.format("enumfacing.side", I18n.format("enumfacing." + accessor.getSide().getName()));
            currenttip.add(TextFormattingHelper.colorizeKeyValue(sideString));

            return currenttip;
        }
    }
}
