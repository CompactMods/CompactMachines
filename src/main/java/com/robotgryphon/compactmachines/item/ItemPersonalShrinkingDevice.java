package com.robotgryphon.compactmachines.item;

import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPersonalShrinkingDevice extends Item {

    public ItemPersonalShrinkingDevice(Properties props) {
        super(props);
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
//            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip." + compactmachines.MODID + ".psd.hint"));
//        } else {
//            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip." + compactmachines.MODID + ".hold_shift.hint"));
//        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand == Hand.OFF_HAND) {
            return ActionResult.resultFail(stack);
        }

//        TODO: Machine enter/exit dimension
//        if(world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
//            // player.openGui(compactmachines.instance, GuiIds.PSD_GUIDE.ordinal(), world, (int) player.posX, (int) player.posY, (int) player.posZ);
//            return new ActionResult(ActionResultType.SUCCESS, stack);
//        }
//
        if (!world.isRemote && player instanceof ServerPlayerEntity) {

            // DimensionalUtil.teleportEntity(pe, registrykey, 8, 6, 8);

            if (player.isSneaking()) {
//                int coords = StructureTools.getCoordsForPos(player.getPosition());
//                Vec3d pos = player.getPositionVector();
//                WorldSavedDataMachines.INSTANCE.addSpawnPoint(coords, pos.x, pos.y, pos.z);
//
//                TextComponentTranslation tc = new TextComponentTranslation("item.compactmachines.psd.spawnpoint_set");
//                tc.getStyle().setColor(TextFormatting.GREEN);
//                player.sendStatusMessage(tc, false);
            } else {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                if(serverPlayer.world.getDimensionKey() == Registrations.COMPACT_DIMENSION) {
                    // Try teleport to compact machine dimension
                    ServerWorld ovw = world.getServer().getWorld(World.OVERWORLD);
                    serverPlayer.teleport(ovw, 8.5, 6, 8.5, serverPlayer.rotationYaw, serverPlayer.rotationPitch);
                }

                // TeleportationTools.teleportPlayerOutOfMachine(serverPlayer);
            }
        }

        return ActionResult.resultSuccess(stack);
    }
}
