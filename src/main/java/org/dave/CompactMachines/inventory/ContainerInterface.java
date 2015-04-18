package org.dave.CompactMachines.inventory;

import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;

import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityInterface;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerInterface extends ContainerCM {
	private TileEntityInterface	tileEntityInterface;

	public ContainerInterface(InventoryPlayer inventoryPlayer, TileEntityInterface tileEntityInterface) {
		this.tileEntityInterface = tileEntityInterface;

		this.addSlotToContainer(new Slot(tileEntityInterface, 0, 80, 45));

		// Add the player's inventory slots to the container
		for (int inventoryRowIndex = 0; inventoryRowIndex < PLAYER_INVENTORY_ROWS; ++inventoryRowIndex)
		{
			for (int inventoryColumnIndex = 0; inventoryColumnIndex < PLAYER_INVENTORY_COLUMNS; ++inventoryColumnIndex)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 106 + inventoryRowIndex * 18));
			}
		}

		// Add the player's action bar slots to the container
		for (int actionBarSlotIndex = 0; actionBarSlotIndex < PLAYER_INVENTORY_COLUMNS; ++actionBarSlotIndex)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 164));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int var, int value)
	{
		super.updateProgressBar(var, value);

		if (var == 31) {
			tileEntityInterface._fluidamount = value;
		} else if (var == 32) {
			tileEntityInterface._fluidid = value;
		} else if (var == 33) {
			tileEntityInterface._energy = value;
		} else if (var == 34) {
			tileEntityInterface._hoppingmode = value;
		} else if (var == 35) {
			tileEntityInterface._gasamount = value;
		} else if (var == 36) {
			tileEntityInterface._gasid = value;
		} else if (var == 37) {
			tileEntityInterface._mana = value;
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		FluidTankInfo[] tanks = tileEntityInterface.getTankInfo(ForgeDirection.UNKNOWN);
		int n = tanks.length;
		if (n != 0) {
			FluidTankInfo tank = tanks[0];
			if (tank != null) {
				for (int i = 0; i < crafters.size(); i++) {
					if (tank.fluid != null && tank.fluid.amount != 0) {
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 31, tank.fluid.amount);
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 32, tank.fluid.getFluidID());
					} else {
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 31, 0);
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 32, 0);
					}
				}
			}
		}

		if(Reference.MEK_AVAILABLE) {
			GasStack gasContents = tileEntityInterface.getGasContents();

			if (gasContents != null) {
				for (int i = 0; i < crafters.size(); i++) {
					if (gasContents.amount > 0) {
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 35, gasContents.amount);
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 36, gasContents.getGas().getID());
					} else {
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 35, 0);
						((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 36, 0);
					}
				}
			}
		}

		for (int i = 0; i < crafters.size(); i++) {
			((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 33, tileEntityInterface.getEnergyStored(ForgeDirection.UNKNOWN));
			((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 34, tileEntityInterface.getHoppingMode(ForgeDirection.UNKNOWN));

			if(Reference.BOTANIA_AVAILABLE) {
				((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 37, tileEntityInterface.getCurrentMana());
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int slotIndex)
	{
		ItemStack itemStack = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack())
		{
			ItemStack slotItemStack = slot.getStack();
			itemStack = slotItemStack.copy();

			/**
			 * If we are shift-clicking an item out of the Interface's
			 * container,
			 * attempt to put it in the first available slot in the player's
			 * inventory
			 */
			if (slotIndex < 1)
			{
				if (!this.mergeItemStack(slotItemStack, 1, inventorySlots.size(), false))
				{
					return null;
				}
			}
			else
			{
				/**
				 * Finally, attempt to put stack into the input slot
				 */
				if (!this.mergeItemStack(slotItemStack, 0, 1, false))
				{
					return null;
				}
			}

			if (slotItemStack.stackSize == 0)
			{
				slot.putStack(null);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemStack;
	}
}
