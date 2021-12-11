package dev.compactmods.machines.item;

import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.client.gui.PersonalShrinkingDeviceScreen;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class ItemPersonalShrinkingDevice extends Item {

    public ItemPersonalShrinkingDevice(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (Screen.hasShiftDown()) {
            tooltip.add(TranslationUtil.tooltip(Tooltips.Details.PERSONAL_SHRINKING_DEVICE)
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .withStyle(ChatFormatting.ITALIC));
        }

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        // If we aren't in the compact dimension, allow PSD guide usage
        // Prevents misfiring if a player is trying to leave a machine or set their spawn
        if (world.isClientSide && world.dimension() != Registration.COMPACT_DIMENSION) {
            PersonalShrinkingDeviceScreen.show();
            return InteractionResultHolder.success(stack);
        }

        if (world instanceof ServerLevel && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;

            if (serverPlayer.level.dimension() == Registration.COMPACT_DIMENSION) {
                ServerLevel serverWorld = serverPlayer.getLevel();
                if (player.isShiftKeyDown()) {
                    ChunkPos machineChunk = new ChunkPos(player.blockPosition());

                    CompactRoomData intern = CompactRoomData.get(serverWorld.getServer());
                    if (intern != null) {
                        // Use internal data to set new spawn point
                        intern.setSpawn(machineChunk, player.position());

                        MutableComponent tc = TranslationUtil.message(Messages.MACHINE_SPAWNPOINT_SET)
                                .withStyle(ChatFormatting.GREEN);

                        player.displayClientMessage(tc, true);
                    }
                } else {
                    PlayerUtil.teleportPlayerOutOfMachine(serverWorld, serverPlayer);
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }
}