package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.client.gui.PersonalShrinkingDeviceScreen;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PersonalShrinkingDevice extends Item {

    public PersonalShrinkingDevice(Properties props) {
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
        if (world.isClientSide && !world.dimension().equals(CompactDimension.LEVEL_KEY)) {
            PersonalShrinkingDeviceScreen.show();
            return InteractionResultHolder.success(stack);
        }

        if (world instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            ServerLevel playerDim = serverPlayer.getLevel();
            if (playerDim.dimension().equals(CompactDimension.LEVEL_KEY)) {
                if (player.isShiftKeyDown()) {
                    ChunkPos machineChunk = new ChunkPos(player.blockPosition());

                    final CompactRoomData intern = CompactRoomData.get(playerDim);

                    // Use internal data to set new spawn point
                    intern.setSpawn(machineChunk, player.position(), player.getRotationVector());

                    MutableComponent tc = TranslationUtil.message(Messages.ROOM_SPAWNPOINT_SET)
                            .withStyle(ChatFormatting.GREEN);

                    player.displayClientMessage(tc, true);

                } else {
                    PlayerUtil.teleportPlayerOutOfMachine(playerDim, serverPlayer);
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }
}
