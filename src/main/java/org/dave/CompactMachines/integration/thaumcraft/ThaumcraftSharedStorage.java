package org.dave.CompactMachines.integration.thaumcraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractHoppingStorage;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "thaumcraft.api.aspects.IEssentiaTransport", modid = "Thaumcraft")
public class ThaumcraftSharedStorage extends AbstractHoppingStorage implements IEssentiaTransport {
	private static Map <String, Integer> aspectToID = new HashMap<String, Integer>();
	private static Map <Integer, String> idToAspect = new HashMap<Integer, String>();
	protected Aspect aspect;
	protected int aspectAmount;

	public ThaumcraftSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		max_cooldown = ConfigurationHandler.cooldownEssentia;

		this.side = side;
		this.coord = coord;
		super.setHoppingMode(4);
		if(aspectToID.isEmpty()) {
			ArrayList<String> aspectNames = new ArrayList<String>();
			for(Aspect aspect : Aspect.aspects.values()) {
				aspectNames.add(aspect.getTag());
			}
			Collections.sort(aspectNames);
			int id = 0;

			for(String aspect : aspectNames) {
				aspectToID.put(aspect, id);
				idToAspect.put(id++, aspect);
			}
		}
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = new NBTTagCompound();
		if(aspect != null) {
			compound.setString("Aspect", aspect.getTag());
			compound.setInteger("AspectAmount", aspectAmount);
		}
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		aspect = Aspect.getAspect(tag.getString("Aspect"));
		aspectAmount = tag.getInteger("AspectAmount");
	}

	@Override
	public String type() {
		return "thaumcraft";
	}

	@Override
	public void setHoppingMode(int mode) {}

	@Override
	public boolean isConnectable(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canInputFrom(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canOutputTo(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return false;
		}
		return true;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {}

	@Override
	public Aspect getSuctionType(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return null;
		}
		return aspect == null ? null : aspect;
	}

	@Override
	public int getSuctionAmount(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return 0;
		}
		return ConfigurationHandler.capacityEssentia >= aspectAmount ? 48 : 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
		if(ConfigurationHandler.enableIntegrationThaumcraft) {
			if((aspect == null || aspect == this.aspect) && amount <= aspectAmount) {
				aspectAmount -= amount;
				if(aspectAmount <= 0) {
					this.aspect = null;
					aspectAmount = 0;
				}
				setDirty();
				return amount;
			}
		}
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
		if(ConfigurationHandler.enableIntegrationThaumcraft) {
			if((this.aspect == null || aspect == this.aspect)) {
				this.aspect = aspect;
				int amountToAdd = Math.min(amount, ConfigurationHandler.capacityEssentia - aspectAmount);
				aspectAmount += amountToAdd;
				setDirty();
				return amountToAdd;
			}
		}
		return 0;
	}

	@Override
	public Aspect getEssentiaType(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return null;
		}
		return aspect;
	}

	@Override
	public int getEssentiaAmount(ForgeDirection face) {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return 0;
		}
		return aspectAmount;
	}

	@Override
	public int getMinimumSuction() {
		if(!ConfigurationHandler.enableIntegrationThaumcraft) {
			return Integer.MAX_VALUE;
		}
		return 48;
	}

	@Override
	public boolean renderExtendedTube() {
		return false;
	}

	@Override
	public void hopToTileEntity(TileEntity target, boolean useOppositeSide) {
		if (aspectAmount >= ConfigurationHandler.capacityEssentia || !(target instanceof IEssentiaTransport)) {
			return;
		}

		IEssentiaTransport et = (IEssentiaTransport) target;

		ForgeDirection hoppingSide = ForgeDirection.getOrientation(side);
		if (useOppositeSide) {
			hoppingSide = hoppingSide.getOpposite();
		}

		if (et.isConnectable(hoppingSide.getOpposite()) && et.canOutputTo(hoppingSide.getOpposite())) {
			Aspect ta = null;

			if ((aspect != null) && (aspectAmount > 0)) {
				ta = aspect;
			} else if ((et.getEssentiaAmount(hoppingSide.getOpposite()) > 0) && 
					(et.getSuctionAmount(hoppingSide.getOpposite()) < getSuctionAmount(hoppingSide)) && (getSuctionAmount(hoppingSide) >= et.getMinimumSuction())) {
				ta = et.getEssentiaType(hoppingSide.getOpposite());
			}
			if ((ta != null) && (et.getSuctionAmount(hoppingSide.getOpposite()) < getSuctionAmount(hoppingSide))) {
				
				addEssentia(ta, et.takeEssentia(ta, 1, hoppingSide.getOpposite()), hoppingSide);
			}
		}
	}

	public static int getIDForAspect(Aspect aspect) {
		return aspectToID.get(aspect.getTag());
	}

	public static Aspect getAspectForID(int ID) {
		return Aspect.getAspect(idToAspect.get(ID));
	}
}
