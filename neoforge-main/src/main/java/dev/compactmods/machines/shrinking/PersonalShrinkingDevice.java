package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.shrinking.component.ShrinkingDeviceConfiguration;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.i18n.Translations;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.i18n.RoomTranslations;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PersonalShrinkingDevice extends Item {

    public PersonalShrinkingDevice(Properties props) {
        super(props);
    }

    public static ShrinkingDeviceConfiguration config(ItemStack stack) {
        return stack.getOrDefault(Shrinking.DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltips, flags);
        tooltips.add(Screen.hasShiftDown() ? Translations.UNBREAKABLE_BLOCK.get() : Translations.HINT_HOLD_SHIFT.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        final var config = config(stack);
        if (CompactDimension.isInServerDimension(player) && player instanceof ServerPlayer serverPlayer) {
            // Player Sneaking - Set Room Spawn
            if (player.isShiftKeyDown()) {
                final var roomCode = CompactMachines.roomApi().chunkManager()
                        .findRoomByChunk(serverPlayer.chunkPosition())
                        .orElseThrow();

                final var spawnManager = CompactMachines.roomApi().spawnManager(roomCode);
                spawnManager.setPlayerSpawn(serverPlayer);

                player.displayClientMessage(RoomTranslations.ROOM_SPAWNPOINT_SET.apply(serverPlayer, roomCode), true);
            }

            // Player Not Sneaking - Teleport from Room
            else {
                RoomHelper.teleportPlayerOutOfRoom(serverPlayer).thenAccept(result -> {
                    // Check Result - If successful, maybe attempt to damage the PSD item
                    if (result.successful() && world.getGameRules().getBoolean(CMGameRules.DAMAGE_PSD_ITEMS_ON_ROOM_EXIT)) {
                        handleSuccessfulAtomicShift(stack, serverPlayer, config);
                    }
                });
            }
        }

        return InteractionResultHolder.success(stack);
    }

    public static void handleSuccessfulAtomicShift(ItemStack stack, ServerPlayer serverPlayer, ShrinkingDeviceConfiguration config) {
        switch (config.afterUseAction()) {
            case DAMAGE:
                if(!serverPlayer.hasInfiniteMaterials()) {
                    stack.hurtAndBreak(1, serverPlayer.serverLevel(), serverPlayer, item -> {
                        // RIP, hope you have spare crafting materials nearby!
                    });
                }
                break;

            case BREAK:
                if(!serverPlayer.hasInfiniteMaterials()) {
                    stack.consume(1, serverPlayer);
                    PlayerUtil.breakItemEffect(serverPlayer, stack);
                }
                break;
        }
    }
}
