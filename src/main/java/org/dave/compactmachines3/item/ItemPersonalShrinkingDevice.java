package org.dave.compactmachines3.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPersonalShrinkingDevice extends Item {

    public ItemPersonalShrinkingDevice() {
        super(new Item.Properties().maxStackSize(1));
        // TODO: Creative tab
    }

//    TODO Model locations
//    @SideOnly(MixinEnvironment.Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

//        TODO Show tooltip when shift held
//        if(GuiScreen.isShiftKeyDown()) {
//            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip." + CompactMachines3.MODID + ".psd.hint"));
//        } else {
//            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip." + CompactMachines3.MODID + ".hold_shift.hint"));
//        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(hand == Hand.OFF_HAND) {
            return new ActionResult(ActionResultType.FAIL, stack);
        }

//        TODO: Machine enter/exit dimension
//        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
//            // player.openGui(CompactMachines3.instance, GuiIds.PSD_GUIDE.ordinal(), world, (int) player.posX, (int) player.posY, (int) player.posZ);
//            return new ActionResult(ActionResultType.SUCCESS, stack);
//        }
//
//        if(!world.isRemote && player instanceof ServerPlayerEntity) {
//            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
//
//            if(player.isSneaking()) {
//                int coords = StructureTools.getCoordsForPos(player.getPosition());
//                Vec3d pos = player.getPositionVector();
//                WorldSavedDataMachines.INSTANCE.addSpawnPoint(coords, pos.x, pos.y, pos.z);
//
//                TextComponentTranslation tc = new TextComponentTranslation("item.compactmachines3.psd.spawnpoint_set");
//                tc.getStyle().setColor(TextFormatting.GREEN);
//                player.sendStatusMessage(tc, false);
//            } else {
//                TeleportationTools.teleportPlayerOutOfMachine(serverPlayer);
//            }
//        }

        return new ActionResult(ActionResultType.SUCCESS, stack);
    }
}
