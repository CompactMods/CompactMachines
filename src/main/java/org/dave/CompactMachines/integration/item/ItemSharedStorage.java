package org.dave.CompactMachines.integration.item;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.utility.ItemHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemSharedStorage extends AbstractSharedStorage implements IInventory {
	private int size = 1;
	private ItemStack[] items;

	public ItemSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);
		empty();
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
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag("Items", ItemHelper.writeItemStacksToTag(items));
        compound.setByte("size", (byte) size);

		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
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

}
