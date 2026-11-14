package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.RoomTranslations;
import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class PersonalShrinkingDevice extends Item {

    public static final ResourceKey<Item> RESOURCE_KEY = ResourceKey.create(Registries.ITEM, CompactMachinesCore.identifier("personal_shrinking_device"));

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
    public @NotNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.FAIL;
        }

        final var config = config(stack);
        if (CompactDimension.isInServerDimension(player) && player instanceof ServerPlayer serverPlayer) {
            final var server = level.getServer();

            final var chunks = RoomCapabilities.CHUNK_MANAGER.getCapability(server);
            final var registry = RoomCapabilities.REGISTRY.getCapability(server);
            if (registry == null || chunks == null)
                return InteractionResult.PASS;

            chunks.findRoomByChunk(serverPlayer.chunkPosition()).ifPresent(room -> {

                // Player Sneaking - Set Room Spawn
                if (player.isShiftKeyDown()) {
                    final var spawnManager = room.getCapability(RoomCapabilities.SPAWN_MANAGER);
                    spawnManager.setPlayerSpawn(serverPlayer);

                    player.sendOverlayMessage(RoomTranslations.ROOM_SPAWNPOINT_SET.apply(serverPlayer, room.code()));
                }

                // Player Not Sneaking - Teleport from Room
                else {
                    var shrink = serverPlayer.getCapability(Shrinking.SHRINK, room);
                    if(shrink == null)
                        return;

                    shrink.tryExit().thenAccept(result -> {
                        // Check Result - If successful, maybe attempt to damage the PSD item
                        if (result.successful() && serverPlayer.level().getGameRules().get(Shrinking.GameRules.DAMAGE_PSD_ITEMS_ON_ROOM_EXIT.get())) {
                            ShrinkingHelper.handleSuccessfulAtomicShift(serverPlayer, stack);
                        }
                    });
                }
            });
        }

        return InteractionResult.SUCCESS;
    }


}
