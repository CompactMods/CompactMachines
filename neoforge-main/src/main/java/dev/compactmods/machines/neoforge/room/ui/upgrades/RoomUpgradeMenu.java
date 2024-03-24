package dev.compactmods.machines.neoforge.room.ui.upgrades;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.neoforge.client.render.ConditionalGhostSlot;
import dev.compactmods.machines.neoforge.data.RoomAttachmentDataManager;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.room.upgrade.NeoforgeRoomUpgradeInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RoomUpgradeMenu extends AbstractContainerMenu {
    private final Inventory playerInv;
    final RoomInstance room;

    protected RoomUpgradeMenu(int winId, Inventory playerInv, RoomInstance room, NeoforgeRoomUpgradeInventory upgradeInv) {
        super(Rooms.ROOM_UPGRADE_MENU.get(), winId);
        this.playerInv = playerInv;
        this.room = room;

        // Room inventory
        for(int slot = 0; slot < 9; slot++) {
            int slotX = 8 + slot * 18;

            this.addSlot(new SlotItemHandler(upgradeInv, slot, slotX, 18) {
                @Override
                public void onTake(Player pPlayer, ItemStack pStack) {
                    // TODO: Room Upgrade removal event
                    super.onTake(pPlayer, pStack);
                }
            });
        }

        int slotY = 38 + 31;

        // Main Inventory
        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new ConditionalGhostSlot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, l * 18 + slotY));
            }
        }

        // Hotbar
        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new ConditionalGhostSlot(playerInv, i1, 8 + i1 * 18, slotY + (18 * 3) + 4));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int pIndex) {
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
                return Component.translatable(Constants.MOD_ID + ".ui.room_upgrades");
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int winId, Inventory inventory, Player player) {
                var serverUpgInv = RoomAttachmentDataManager.instance()
                        .orElseThrow()
                        .data(room.code())
                        .getData(Rooms.UPGRADE_INV);

                return new RoomUpgradeMenu(winId, inventory, room, serverUpgInv);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        };
    }

    public static RoomUpgradeMenu createClientMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        final var code = extraData.readUtf();
        final var data = RoomApi.room(code).orElseThrow();

        return new RoomUpgradeMenu(id, playerInv, data, NeoforgeRoomUpgradeInventory.EMPTY);
    }
}
