package org.dave.CompactMachines.inventory;

import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;

import org.dave.CompactMachines.integration.thaumcraft.ThaumcraftSharedStorage;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

import thaumcraft.api.aspects.Aspect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerMachine extends ContainerCM {
	private TileEntityMachine	tileEntityMachine;

	private static int[]		xPositions	= new int[] { 80, 80, 80, 109, 109, 51 };
	private static int[]		yPositions	= new int[] { 66, 24, 45, 66, 45, 45 };

	public ContainerMachine(InventoryPlayer inventoryPlayer, TileEntityMachine tileEntityMachine) {
		this.tileEntityMachine = tileEntityMachine;

		for (int i = 0; i < 6; i++) {
			this.addSlotToContainer(new Slot(tileEntityMachine, i, xPositions[i], yPositions[i])); // DOWN
		}

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

		if (var == 40) {
			tileEntityMachine._fluidamount[0] = value;
		} else if (var == 41) {
			tileEntityMachine._fluidamount[1] = value;
		} else if (var == 42) {
			tileEntityMachine._fluidamount[2] = value;
		} else if (var == 43) {
			tileEntityMachine._fluidamount[3] = value;
		} else if (var == 44) {
			tileEntityMachine._fluidamount[4] = value;
		} else if (var == 45) {
			tileEntityMachine._fluidamount[5] = value;
		} else if (var == 50) {
			tileEntityMachine._fluidid[0] = value;
		} else if (var == 51) {
			tileEntityMachine._fluidid[1] = value;
		} else if (var == 52) {
			tileEntityMachine._fluidid[2] = value;
		} else if (var == 53) {
			tileEntityMachine._fluidid[3] = value;
		} else if (var == 54) {
			tileEntityMachine._fluidid[4] = value;
		} else if (var == 55) {
			tileEntityMachine._fluidid[5] = value;
		} else if (var == 60) {
			tileEntityMachine._energy[0] = value;
		} else if (var == 61) {
			tileEntityMachine._energy[1] = value;
		} else if (var == 62) {
			tileEntityMachine._energy[2] = value;
		} else if (var == 63) {
			tileEntityMachine._energy[3] = value;
		} else if (var == 64) {
			tileEntityMachine._energy[4] = value;
		} else if (var == 65) {
			tileEntityMachine._energy[5] = value;
		} else if (var == 66) {
			tileEntityMachine._gasamount[0] = value;
		} else if (var == 67) {
			tileEntityMachine._gasamount[1] = value;
		} else if (var == 68) {
			tileEntityMachine._gasamount[2] = value;
		} else if (var == 69) {
			tileEntityMachine._gasamount[3] = value;
		} else if (var == 70) {
			tileEntityMachine._gasamount[4] = value;
		} else if (var == 71) {
			tileEntityMachine._gasamount[5] = value;
		} else if (var == 72) {
			tileEntityMachine._gasid[0] = value;
		} else if (var == 73) {
			tileEntityMachine._gasid[1] = value;
		} else if (var == 74) {
			tileEntityMachine._gasid[2] = value;
		} else if (var == 75) {
			tileEntityMachine._gasid[3] = value;
		} else if (var == 76) {
			tileEntityMachine._gasid[4] = value;
		} else if (var == 77) {
			tileEntityMachine._gasid[5] = value;
		} else if (var == 80) {
			tileEntityMachine._mana = value;
		} else if (var == 81) {
			tileEntityMachine._aspectamount[0] = value;
		} else if (var == 82) {
			tileEntityMachine._aspectamount[1] = value;
		} else if (var == 83) {
			tileEntityMachine._aspectamount[2] = value;
		} else if (var == 84) {
			tileEntityMachine._aspectamount[3] = value;
		} else if (var == 85) {
			tileEntityMachine._aspectamount[4] = value;
		} else if (var == 86) {
			tileEntityMachine._aspectamount[5] = value;
		} else if (var == 87) {
			tileEntityMachine._aspectid[0] = value;
		} else if (var == 88) {
			tileEntityMachine._aspectid[1] = value;
		} else if (var == 89) {
			tileEntityMachine._aspectid[2] = value;
		} else if (var == 90) {
			tileEntityMachine._aspectid[3] = value;
		} else if (var == 91) {
			tileEntityMachine._aspectid[4] = value;
		} else if (var == 92) {
			tileEntityMachine._aspectid[5] = value;
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if(Reference.BOTANIA_AVAILABLE) {
			for (int i = 0; i < crafters.size(); i++) {
				((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 80, tileEntityMachine.getCurrentMana());
			}
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			FluidTankInfo[] tanks = tileEntityMachine.getTankInfo(dir);
			int n = tanks.length;
			if (n != 0) {
				FluidTankInfo tank = tanks[0];
				if (tank != null) {
					for (int i = 0; i < crafters.size(); i++) {
						if (tank.fluid != null && tank.fluid.amount != 0) {
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 40 + dir.ordinal(), tank.fluid.amount);
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 50 + dir.ordinal(), tank.fluid.getFluidID());
						} else {
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 40 + dir.ordinal(), 0);
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 50 + dir.ordinal(), 0);
						}
					}
				}
			}

			if(Reference.MEK_AVAILABLE) {
				GasStack gasContents = tileEntityMachine.getGasContents(dir);

				if (gasContents != null) {
					for (int i = 0; i < crafters.size(); i++) {
						if (gasContents.amount > 0) {
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 66 + dir.ordinal(), gasContents.amount);
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 72 + dir.ordinal(), gasContents.getGas().getID());
						} else {
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 66 + dir.ordinal(), 0);
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 72 + dir.ordinal(), 0);
						}
					}
				}
			}

			if(Reference.THAUMCRAFT_AVAILABLE) {
				Aspect aspect = tileEntityMachine.getEssentiaType(dir);
				int aspectAmount = tileEntityMachine.getEssentiaAmount(dir);

				if (aspect != null) {
					for (int i = 0; i < crafters.size(); i++) {
						if (aspectAmount > 0) {
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 81 + dir.ordinal(), aspectAmount);
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 87 + dir.ordinal(), ThaumcraftSharedStorage.getIDForAspect(aspect));
						} else {
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 81 + dir.ordinal(), 0);
							((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 87 + dir.ordinal(), 0);
						}
					}
				}
			}

			for (int i = 0; i < crafters.size(); i++) {
				((ICrafting) crafters.get(i)).sendProgressBarUpdate(this, 60 + dir.ordinal(), tileEntityMachine.getEnergyStored(dir));
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int slotIndex)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			int chestSlots = 6;
			if (slotIndex < chestSlots)
			{
				if (!mergeItemStack(itemstack1, chestSlots, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (!mergeItemStack(itemstack1, 0, chestSlots, false))
			{
				return null;
			}
			if (itemstack1.stackSize == 0)
			{
				slot.putStack(null);
			} else
			{
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}
}
