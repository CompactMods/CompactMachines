package org.dave.CompactMachines.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;

import org.dave.CompactMachines.integration.AbstractBufferedStorage;
import org.dave.CompactMachines.integration.AbstractHoppingStorage;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.integration.appeng.AESharedStorage;
import org.dave.CompactMachines.integration.botania.BotaniaSharedStorage;
import org.dave.CompactMachines.integration.bundledredstone.BRSharedStorage;
import org.dave.CompactMachines.integration.fluid.FluidSharedStorage;
import org.dave.CompactMachines.integration.gas.GasSharedStorage;
import org.dave.CompactMachines.integration.item.ItemSharedStorage;
import org.dave.CompactMachines.integration.opencomputers.OpenComputersSharedStorage;
import org.dave.CompactMachines.integration.pneumaticcraft.PneumaticCraftSharedStorage;
import org.dave.CompactMachines.integration.redstoneflux.FluxSharedStorage;
import org.dave.CompactMachines.integration.thaumcraft.ThaumcraftSharedStorage;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.utility.LogHelper;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SharedStorageHandler {
	private static SharedStorageHandler					serverStorageHandler;
	private static SharedStorageHandler					clientStorageHandler;

	private Map<String, AbstractSharedStorage>			storageMap;
	private Map<String, List<AbstractSharedStorage>>	storageList;
	private Map<String, Class> classMap;

	public final boolean								client;

	private File										saveDir;
	private File[]										saveFiles;
	private int											saveTo;

	private List<AbstractSharedStorage>					dirtyStorage;
	private NBTTagCompound								saveTag;

	public static SharedStorageHandler instance(boolean client) {
		return client ? clientStorageHandler : serverStorageHandler;
	}

	public SharedStorageHandler(boolean client) {
		this.client = client;

		storageMap = Collections.synchronizedMap(new HashMap<String, AbstractSharedStorage>());
		storageList = Collections.synchronizedMap(new HashMap<String, List<AbstractSharedStorage>>());
		dirtyStorage = Collections.synchronizedList(new LinkedList<AbstractSharedStorage>());
		classMap = Collections.synchronizedMap(new HashMap<String, Class>());

		registerModInteraction("item", ItemSharedStorage.class);
		registerModInteraction("liquid", FluidSharedStorage.class);
		registerModInteraction("flux", FluxSharedStorage.class);

		if(Reference.MEK_AVAILABLE) {
			registerModInteraction("gas", GasSharedStorage.class);
		}

		if(Reference.AE_AVAILABLE) {
			registerModInteraction("appeng", AESharedStorage.class);
		}

		if(Reference.PR_AVAILABLE) {
			registerModInteraction("bundledRedstone", BRSharedStorage.class);
		}

		if(Reference.OC_AVAILABLE) {
			registerModInteraction("OpenComputers", OpenComputersSharedStorage.class);
		}

		if(Reference.BOTANIA_AVAILABLE) {
			registerModInteraction("botania", BotaniaSharedStorage.class);
		}

		if(Reference.THAUMCRAFT_AVAILABLE) {
			registerModInteraction("thaumcraft", ThaumcraftSharedStorage.class);
		}

		if(Reference.PNEUMATICCRAFT_AVAILABLE) {
			registerModInteraction("PneumaticCraft", PneumaticCraftSharedStorage.class);
		}

		if (!client) {
			load();
		}
	}

	public void registerModInteraction(String key, Class integrationClass) {
		if(storageList.containsKey(key)) {
			LogHelper.error("Double registration of abstract storage: " + integrationClass);
			return;
		}

		storageList.put(key, new ArrayList<AbstractSharedStorage>());
		classMap.put(key, integrationClass);
	}

	public void requestSave(AbstractSharedStorage storage) {
		dirtyStorage.add(storage);
	}

	public static void reloadStorageHandler(boolean client) {
		SharedStorageHandler newHandler = new SharedStorageHandler(client);
		if (client) {
			clientStorageHandler = newHandler;
		} else {
			serverStorageHandler = newHandler;
		}
	}

	public void setHoppingMode(int coord, int side, String type, int hoppingMode) {
		AbstractSharedStorage storage = getStorage(coord, side, type);
		if(storage instanceof AbstractHoppingStorage) {
			((AbstractHoppingStorage)storage).setHoppingMode(hoppingMode);
			((AbstractHoppingStorage)storage).setDirty();
		}
	}

	public void setHoppingModeForAll(int coord, int side, int hoppingMode) {
		for(String key : storageList.keySet()) {
			setHoppingMode(coord, side, key, hoppingMode);
		}
	}

	public boolean storageExists(int coord, int side, int entangledInstance, String type) {
		String key = coord + "|" + side + "|" + type;

		return storageMap.containsKey(key);
	}

	public List<AbstractSharedStorage> getAllStorages(int coord, int side) {
		List<AbstractSharedStorage> result = Lists.newArrayList();
		for(String key : storageList.keySet()) {
			result.add(getStorage(coord, side, key));
		}

		return result;
	}

	public AbstractSharedStorage getStorage(int coord, int side, String type) {
		return getStorage(coord, side, 0, type);
	}

	public AbstractSharedStorage getStorage(int coord, int side, int entangledInstance, String type) {
		String key = coord + "|" + side + "|" + type;

		AbstractSharedStorage storage = storageMap.get(key);
		if (storage == null) {
			Class<?> storageClass = classMap.get(type);
			if(storageClass == null) {
				return null;
			}

			try {
				Constructor constructor = storageClass.getConstructor(SharedStorageHandler.class, Integer.TYPE, Integer.TYPE);
				storage = (AbstractSharedStorage) constructor.newInstance(this, coord, side);
			} catch (Exception e) {
				LogHelper.error("Could not create instance of class: " + storageClass);
				e.printStackTrace();
				return null;
			}

			if (!client && saveTag.hasKey(key) && storage instanceof AbstractBufferedStorage) {
				((AbstractBufferedStorage)storage).loadFromTag(saveTag.getCompoundTag(key));
			}

			storageMap.put(key, storage);
			storageList.get(type).add(storage);
		}

		return storage;
	}

	private void load() {
		saveDir = new File(DimensionManager.getCurrentSaveRootDirectory(), Reference.MOD_ID);
		try {
			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}

			saveFiles = new File[] {
					new File(saveDir, "data1.dat"),
					new File(saveDir, "data2.dat"),
					new File(saveDir, "lock.dat")
			};

			if (saveFiles[2].exists() && saveFiles[2].length() > 0) {
				FileInputStream fin = new FileInputStream(saveFiles[2]);
				saveTo = fin.read() ^ 1;
				fin.close();

				if (saveFiles[saveTo ^ 1].exists()) {
					DataInputStream din = new DataInputStream(new FileInputStream(saveFiles[saveTo ^ 1]));
					saveTag = CompressedStreamTools.readCompressed(din);
					din.close();
				} else {
					saveTag = new NBTTagCompound();
				}
			} else {
				saveTag = new NBTTagCompound();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void save(boolean force) {
		if (!dirtyStorage.isEmpty() || force) {
			for (AbstractSharedStorage inv : dirtyStorage) {

				String key = inv.coord + "|" + inv.side + "|" + inv.type();
				saveTag.setTag(key, ((AbstractBufferedStorage)inv).saveToTag());
				((AbstractBufferedStorage)inv).setClean();
			}

			dirtyStorage.clear();
			try {
				File saveFile = saveFiles[saveTo];
				if (!saveFile.exists()) {
					saveFile.createNewFile();
				}

				DataOutputStream dout = new DataOutputStream(new FileOutputStream(saveFile));
				CompressedStreamTools.writeCompressed(saveTag, dout);
				dout.close();

				FileOutputStream fout = new FileOutputStream(saveFiles[2]);
				fout.write(saveTo);
				fout.close();
				saveTo ^= 1;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class SharedStorageSaveHandler
	{
		@SubscribeEvent
		public void onWorldLoad(Load event) {
			if (event.world.isRemote) {
				reloadStorageHandler(true);
			}
		}

		@SubscribeEvent
		public void onWorldSave(Save event) {
			if (!event.world.isRemote && instance(false) != null) {
				instance(false).save(false);
			}
		}
	}
}
