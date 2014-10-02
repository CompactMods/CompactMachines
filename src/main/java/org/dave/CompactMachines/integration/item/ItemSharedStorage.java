package org.dave.CompactMachines.integration.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.ItemHelper;

public class ItemSharedStorage extends AbstractSharedStorage implements IInventory {
	private int size = 1;
	private ItemStack[] items;

	public ItemSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);
		empty();

		max_cooldown = ConfigurationHandler.cooldownItems;
	}

	public void empty() {
		items = new ItemStack[size];
	}

	@Override
	public String type() {
		return "item";
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = prepareTagCompound();
		compound.setTag("Items", ItemHelper.writeItemStacksToTag(items));
		compound.setByte("size", (byte) size);
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		loadHoppingModeFromCompound(tag);
		size = tag.getByte("size");
		empty();
		ItemHelper.readItemStacksFromTag(items, tag.getTagList("Items", 10));
	}

	@Override
	public int getSizeInventory() {
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		synchronized(this)
		{
			return items[slot];
		}
	}

	@Override
	public ItemStack decrStackSize(int slot, int decreaseAmount) {
		synchronized(this)
		{
			return ItemHelper.decrStackSize(this, slot, decreaseAmount);
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		synchronized(this)
		{
			items[slot] = stack;
			markDirty();
		}
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		setDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory() {
		return;
	}

	@Override
	public void closeInventory() {
		return;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}


	private void hopToTileEntity(TileEntity tileEntityOutside) {
		ItemStack stack = getStackInSlot(0);
		if(stack == null || stack.stackSize == 0) {
			return;
		}

		if(cooldown == max_cooldown) {
			cooldown = 0;
		} else {
			cooldown++;
			return;
		}

		int targetSlot = -1;
		if(tileEntityOutside instanceof IInventory) {
			if(tileEntityOutside instanceof ISidedInventory) {
				ISidedInventory inv = (ISidedInventory)tileEntityOutside;
				int[] accessibleSlotsFromSide = inv.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

				for(int slot : accessibleSlotsFromSide) {
					if(inv.isItemValidForSlot(slot, stack) && (inv.getStackInSlot(slot) == null || inv.getStackInSlot(slot).stackSize < inv.getInventoryStackLimit())) {
						targetSlot = slot;
						break;
					}
				}
			} else {
				IInventory inv = (IInventory)tileEntityOutside;

				for(int i = 0; i < inv.getSizeInventory(); i++) {
					if(inv.isItemValidForSlot(i, stack) && (inv.getStackInSlot(i) == null || inv.getStackInSlot(i).stackSize < inv.getInventoryStackLimit())) {
						targetSlot = i;
						break;
					}
				}
			}
		}

		if(targetSlot == -1) {
			return;
		}

		IInventory inv = (IInventory)tileEntityOutside;
		ItemStack targetStack = inv.getStackInSlot(targetSlot);

		int max = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
		if(targetStack == null) {
			// Target slot is empty
			if(stack.stackSize <= max) {
				// We can safely transfer the whole stack
				inv.setInventorySlotContents(targetSlot, stack);
				stack = null;
			} else {
				// Stack needs to be split
				inv.setInventorySlotContents(targetSlot, stack.splitStack(max));
			}
			inv.markDirty();
		} else if(targetStack.isItemEqual(stack)) {
			// Target slot contains the same kind of item
			if(stack.stackSize <= max) {
				int amount = Math.min(stack.stackSize, max - targetStack.stackSize);
				if(amount > 0) {
					targetStack.stackSize += amount;
					stack.stackSize -= amount;
					if(stack.stackSize < 1) {
						stack = null;
					}
					inv.markDirty();
				}
			}
		}

		setInventorySlotContents(0, stack);
	}

	@Override
	public void hopToOutside(TileEntityMachine tileEntityMachine, TileEntity tileEntityOutside) {
		hopToTileEntity(tileEntityOutside);
	}

	@Override
	public void hopToInside(TileEntityInterface tileEntityInterface, TileEntity tileEntityInside) {
		hopToTileEntity(tileEntityInside);
	}

}
