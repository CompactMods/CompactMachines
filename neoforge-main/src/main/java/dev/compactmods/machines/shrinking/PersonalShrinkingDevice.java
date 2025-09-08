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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class PersonalShrinkingDevice extends Item {

    public static final ResourceKey<Item> RESOURCE_KEY = ResourceKey.create(Registries.ITEM, CompactMachines.modRL("personal_shrinking_device"));

    public PersonalShrinkingDevice(Properties props) {
        super(props);
    }

    public static ShrinkingDeviceConfiguration config(ItemStack stack) {
        return stack.getOrDefault(Shrinking.DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
//        tooltipAdder.accept(Screen.hasShiftDown() ? Translations..get() : Translations.HINT_HOLD_SHIFT.get());
    }

    @Override
    public @NotNull InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.FAIL;
        }

        final var config = config(stack);
        if (CompactDimension.isInServerDimension(player) && player instanceof ServerPlayer serverPlayer) {
            // Player Sneaking - Set Room Spawn
            if (player.isShiftKeyDown()) {
                final var roomCode = CompactMachines.chunkManager()
                        .findRoomByChunk(serverPlayer.chunkPosition())
                        .orElseThrow();

                final var spawnManager = CompactMachines.spawnManagers().get(roomCode);
                spawnManager.setPlayerSpawn(serverPlayer);

                player.displayClientMessage(RoomTranslations.ROOM_SPAWNPOINT_SET.apply(serverPlayer, roomCode), true);
            }

            // Player Not Sneaking - Teleport from Room
            else {
                RoomHelper.teleportPlayerOutOfRoom(serverPlayer).thenAccept(result -> {
                    // Check Result - If successful, maybe attempt to damage the PSD item
                    if (result.successful() && world.getServer().getGameRules().getBoolean(CMGameRules.DAMAGE_PSD_ITEMS_ON_ROOM_EXIT)) {
                        handleSuccessfulAtomicShift(stack, serverPlayer, config);
                    }
                });
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static void handleSuccessfulAtomicShift(ItemStack stack, ServerPlayer serverPlayer, ShrinkingDeviceConfiguration config) {
        switch (config.afterUseAction()) {
            case DAMAGE:
                if(!serverPlayer.hasInfiniteMaterials()) {
                    stack.hurtAndBreak(1, serverPlayer.level(), serverPlayer, item -> {
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
