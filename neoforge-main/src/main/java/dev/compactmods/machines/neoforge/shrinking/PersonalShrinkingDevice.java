package dev.compactmods.machines.neoforge.shrinking;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.Messages;
import dev.compactmods.machines.api.Tooltips;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.room.RoomHelper;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
            // fixme PersonalShrinkingDeviceScreen.show();
            return InteractionResultHolder.success(stack);
        }

        if (world instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            ServerLevel playerDim = serverPlayer.serverLevel();
            if (playerDim.dimension().equals(CompactDimension.LEVEL_KEY)) {
                if (player.isShiftKeyDown()) {
                    // FIXME Change Spawnpoint
                    RoomApi.chunkManager()
                            .findRoomByChunk(serverPlayer.chunkPosition())
                            .map(RoomApi::spawnManager)
                            .ifPresent(spawnManager -> {
                                spawnManager.setPlayerSpawn(serverPlayer.getUUID(), player.position(), player.getRotationVector());

                                MutableComponent tc = TranslationUtil.message(Messages.ROOM_SPAWNPOINT_SET)
                                        .withStyle(ChatFormatting.GREEN);

                                player.displayClientMessage(tc, true);

                            });

//                    final var roomInfo = CompactRoomProvider.instance(playerDim);
//                    roomInfo.findByChunk(player.chunkPosition()).ifPresent(room -> {
//                        if(room instanceof IMutableRoomRegistration mutableRoom) {
//                            mutableRoom.setSpawnPosition(player.position());
//                            mutableRoom.setSpawnRotation(PlayerUtil.getLookDirection(player));
//                        }
//                    });
                } else {
                    RoomHelper.teleportPlayerOutOfRoom(serverPlayer);
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }
}
