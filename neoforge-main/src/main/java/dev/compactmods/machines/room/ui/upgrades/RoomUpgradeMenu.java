package dev.compactmods.machines.room.ui.upgrades;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.client.render.ConditionalGhostSlot;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.api.room.upgrade.inventory.RoomUpgradeInventory;
import dev.compactmods.machines.util.SlotRangeUtil;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.HandlerItemAccess;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class RoomUpgradeMenu extends AbstractContainerMenu {
    public final String roomCode;
    public boolean showBackButton = true;

    private final PlayerInventoryWrapper playerInvHandler;
    private final RoomUpgradeInventory upgradeInvHandler;

    private final SlotRange playerInvRange;

    protected RoomUpgradeMenu(int winId, Inventory playerInv, String roomCode, RoomUpgradeInventory upgradeInv) {
        super(Rooms.Menus.ROOM_UPGRADES.get(), winId);
        this.roomCode = roomCode;

        this.playerInvHandler = PlayerInventoryWrapper.of(playerInv);
        this.upgradeInvHandler = upgradeInv;

        // this.upgradeInvRange = SlotRangeUtil.makeSlotRange("components", 0, 9);
        this.playerInvRange = SlotRangeUtil.makeSlotRange("player_inv", 9, 36);

        // Room inventory
        for (int slot = 0; slot < 9; slot++) {
            int slotX = 8 + slot * 18;

            final var s = new ResourceHandlerSlot(this.upgradeInvHandler, this.upgradeInvHandler::set, slot, slotX, 18);
            this.addSlot(s);
        }

        int slotY = 38 + 31;

        // Main Inventory
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new ConditionalGhostSlot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, l * 18 + slotY));
            }
        }

        // Hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new ConditionalGhostSlot(playerInv, i1, 8 + i1 * 18, slotY + (18 * 3) + 4));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int pIndex) {
        final var slot = this.slots.get(pIndex);

        final var stackToMove = slot.getItem();
        if (stackToMove.isEmpty())
            return ItemStack.EMPTY;

        ResourceHandler<ItemResource> source, destination;
        if (playerInvRange.slots().contains(pIndex)) {
            // Move from player to components
            source = RangedResourceHandler.ofSingleIndex(playerInvHandler, slot.getContainerSlot());
            destination = upgradeInvHandler;
        } else {
            // Move from components to player
            source = RangedResourceHandler.ofSingleIndex(upgradeInvHandler, pIndex);
            destination = playerInvHandler;
        }

        ResourceHandlerUtil.move(source, destination, Predicates.alwaysTrue(), stackToMove.getCount(), null);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static MenuProvider provider(RoomInstance room) {
        return new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable(CompactMachines.MOD_ID + ".ui.room_upgrades");
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int winId, Inventory inventory, Player player) {
                var serverUpgInv = room.getData(CMDataAttachments.UPGRADE_ITEMS);
                serverUpgInv.setRoomInstance(room);
                return new RoomUpgradeMenu(winId, inventory, room.code(), serverUpgInv);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        };
    }

    public static RoomUpgradeMenu createClientMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        final var isIsolated = extraData.readBoolean();
        final var code = extraData.readUtf();
        final var inv = RoomUpgradeInventory.STREAM_CODEC.decode(extraData);

        var menu = new RoomUpgradeMenu(id, playerInv, code, inv);
        menu.setIsolated(isIsolated);
        return menu;
    }

    private void setIsolated(boolean isolated) {
        this.showBackButton = !isolated;
    }
}
