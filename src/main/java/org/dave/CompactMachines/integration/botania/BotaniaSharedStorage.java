package org.dave.CompactMachines.integration.botania;

import net.minecraft.nbt.NBTTagCompound;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractBufferedStorage;

import vazkii.botania.api.mana.IManaPool;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "vazkii.botania.api.mana.IManaPool", modid = "Botania")
public class BotaniaSharedStorage extends AbstractBufferedStorage implements IManaPool {

	protected int mana = 0;

	public BotaniaSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		this.side = side;
		this.coord = coord;
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("mana", mana);
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		mana = tag.getInteger("mana");
	}

	@Override
	public String type() {
		return "botania";
	}

	@Override
	public boolean isFull() {
		if(!ConfigurationHandler.enableIntegrationBotania) {
			return true;
		}
		return (ConfigurationHandler.capacityMana <= mana);
	}

	@Override
	public void recieveMana(int mana) {
		if(!ConfigurationHandler.enableIntegrationBotania) {
			return;
		}

		if(mana < 0 || this.mana < ConfigurationHandler.capacityMana) {
			this.mana += mana;
			setDirty();
		}
	}

	@Override
	public boolean canRecieveManaFromBursts() {
		if(!ConfigurationHandler.enableIntegrationBotania) {
			return false;
		}
		return true;
	}

	@Override
	public int getCurrentMana() {
		if(!ConfigurationHandler.enableIntegrationBotania) {
			return 0;
		}
		return mana;
	}

	@Override
	public boolean isOutputtingPower() {
		if(!ConfigurationHandler.enableIntegrationBotania) {
			return false;
		}
		return false;
	}

}
