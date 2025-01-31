package dev.compactmods.machines.room.ui.upgrades;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.client.render.ConditionalGhostSlot;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.api.room.upgrade.inventory.RoomUpgradeInventory;
import dev.compactmods.machines.room.upgrade.RoomUpgradeMenuEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoomUpgradeMenu extends AbstractContainerMenu {
   private final Inventory playerInv;
   public final String roomCode;
   public boolean showBackButton = true;
   private boolean standalone = false;

   protected RoomUpgradeMenu(int winId, Inventory playerInv, String roomCode, RoomUpgradeInventory upgradeInv) {
	  super(Rooms.Menus.ROOM_UPGRADES.get(), winId);
	  this.playerInv = playerInv;
	  this.roomCode = roomCode;

	  // Room inventory
	  for (int slot = 0; slot < 9; slot++) {
		 int slotX = 8 + slot * 18;

		 this.addSlot(new SlotItemHandler(upgradeInv, slot, slotX, 18) {

			 @Override
			 public boolean mayPlace(ItemStack stack) {
				 return stack.has(CMDataComponents.UPGRADE_LIST_COMPONENT);
			 }

			 @Override
			 public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
				 super.setByPlayer(newStack, oldStack);

				 if(oldStack.has(CMDataComponents.UPGRADE_INSTANCE_ID)) {
					 var instanceID = oldStack.get(CMDataComponents.UPGRADE_INSTANCE_ID);
					 RoomUpgradeMenuEvents.onUpgradeRemoved(roomCode, oldStack, instanceID);
				 }

				 if(newStack.isEmpty()) return;

				 if(!newStack.has(CMDataComponents.UPGRADE_INSTANCE_ID)) {
					 UUID newInstanceID = UUID.randomUUID();
					 newStack.set(CMDataComponents.UPGRADE_INSTANCE_ID, newInstanceID);
				 }

				 var instanceID = newStack.get(CMDataComponents.UPGRADE_INSTANCE_ID);
				 RoomUpgradeMenuEvents.onUpgradeApplied(roomCode, newStack, instanceID);
			 }
		 });
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
	  return ItemStack.EMPTY;
   }

   @Override
   public boolean stillValid(Player player) {
	  return true;
   }

   @Override
   public void clicked(int pSlotId, int pButton, ClickType clickType, Player player) {
	  if (player.level().isClientSide)
		 return;

	  super.clicked(pSlotId, pButton, clickType, player);
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
			return new RoomUpgradeMenu(winId, inventory, room.code(), serverUpgInv);
		 }

		 @Override
		 public boolean shouldTriggerClientSideContainerClosingOnOpen() {
			return false;
		 }
	  };
   }

   public static RoomUpgradeMenu createClientMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
	  final var isIsolated = extraData.readBoolean();
	  final var code = extraData.readUtf();

	  var menu = new RoomUpgradeMenu(id, playerInv, code, RoomUpgradeInventory.EMPTY);
	  menu.setIsolated(isIsolated);
	  return menu;
   }

   private void setIsolated(boolean isolated) {
	  this.showBackButton = !isolated;
   }
}
