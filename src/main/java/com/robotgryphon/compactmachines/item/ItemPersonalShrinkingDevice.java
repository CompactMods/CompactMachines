package com.robotgryphon.compactmachines.item;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.client.gui.PersonalShrinkingDeviceScreen;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.util.CompactMachineUtil;
import com.robotgryphon.compactmachines.util.PlayerUtil;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.network.NetworkHooks;

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

        if (Screen.hasShiftDown()) {
            tooltip.add(
                    new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".psd.hint")
                            .mergeStyle(TextFormatting.YELLOW));
        } else {
            tooltip.add(
                    new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".hold_shift.hint")
                            .mergeStyle(TextFormatting.GRAY));
        }

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand == Hand.OFF_HAND) {
            return ActionResult.resultFail(stack);
        }

        // If we aren't in the compact dimension, allow PSD guide usage
        // Prevents misfiring if a player is trying to leave a machine or set their spawn
        if(world.isRemote && world.getDimensionKey() != Registration.COMPACT_DIMENSION) {
            PersonalShrinkingDeviceScreen.show();
            return ActionResult.resultSuccess(stack);
        }

        if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            if (serverPlayer.world.getDimensionKey() == Registration.COMPACT_DIMENSION) {
                    ServerWorld serverWorld = serverPlayer.getServerWorld();
                if (player.isSneaking()) {
                    CompactMachineUtil.setMachineSpawn(serverWorld.getServer(), player.getPosition());

                    IFormattableTextComponent tc = new TranslationTextComponent("messages.compactmachines.psd.spawnpoint_set")
                            .mergeStyle(TextFormatting.GREEN);

                    player.sendStatusMessage(tc, true);
                } else {
                    PlayerUtil.teleportPlayerOutOfMachine(serverWorld, serverPlayer);
                }
            }
        }

        return ActionResult.resultSuccess(stack);
    }

}
