package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
import dev.compactmods.machines.shrinking.history.UsedShrinkingDeviceOnMachine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Entering a machine's room — shared by using a shrinking device on the block and the machine
 * screen's "enter room" button.
 *
 * <p>Allowed for players in creative, operators, or anyone carrying a shrinking device (any item
 * with the {@link Shrinking.DataComponents#SHRINKING_CONFIG shrinking device component}).
 */
public final class MachineRoomEntry {

    private MachineRoomEntry() {}

    /** {@return whether {@code player} may enter a room} Safe on either side. */
    public static boolean mayEnter(Player player) {
        return bypassesDevice(player) || findShrinkingDevice(player).isPresent();
    }

    /** Creative players and operators don't need a shrinking device. */
    public static boolean bypassesDevice(Player player) {
        return player.isCreative() || player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /** {@return the first shrinking device in {@code player}'s inventory}, if any. */
    public static Optional<ItemStack> findShrinkingDevice(Player player) {
        final var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.has(Shrinking.DataComponents.SHRINKING_CONFIG))
                return Optional.of(stack);
        }
        return Optional.empty();
    }

    /**
     * Sends {@code player} into {@code room} through {@code machine}. {@code device} is the shrinking
     * device used, if any — after a successful shift its after-use action (damage, consume) applies;
     * pass {@link ItemStack#EMPTY} for creative/operator entry.
     *
     * @return {@code false} if the player has no shrinking handler for the room
     */
    public static boolean tryEnter(ServerPlayer player, RoomInstance room, CompactMachineBlockEntity machine, ItemStack device) {
        final var shrinkHandler = player.getCapability(Shrinking.SHRINK, room);
        if (shrinkHandler == null) {
            CompactMachinesCore.modLog().error("Error: Could not fetch or create a shrinking handler for room roomCode [{}], player [{}].",
                    room.code(), player.getUUID());
            return false;
        }

        final var method = new UsedShrinkingDeviceOnMachine(player, room, machine.getLevelPosition());
        shrinkHandler.tryEnter(method).thenAccept(result -> {
            if (result.successful())
                ShrinkingHelper.handleSuccessfulAtomicShift(player, device);
        });

        return true;
    }
}
