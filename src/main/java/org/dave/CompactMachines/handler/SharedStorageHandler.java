package org.dave.CompactMachines.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.integration.appeng.AESharedStorage;
import org.dave.CompactMachines.integration.bundledredstone.BRSharedStorage;
import org.dave.CompactMachines.integration.fluid.FluidSharedStorage;
import org.dave.CompactMachines.integration.gas.GasSharedStorage;
import org.dave.CompactMachines.integration.item.ItemSharedStorage;
import org.dave.CompactMachines.integration.opencomputers.OpenComputersSharedStorage;
import org.dave.CompactMachines.integration.redstoneflux.FluxSharedStorage;
import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SharedStorageHandler {
	private static SharedStorageHandler					serverStorageHandler;
	private static SharedStorageHandler					clientStorageHandler;

	private Map<String, AbstractSharedStorage>			storageMap;
	private Map<String, List<AbstractSharedStorage>>	storageList;

	public final boolean								client;

	private File										saveDir;
	private File[]										saveFiles;
	private int											saveTo;

	private List<AbstractSharedStorage>					dirtyStorage;
	private NBTTagCompound								saveTag;

	public SharedStorageHandler(boolean client) {
		this.client = client;

		storageMap = Collections.synchronizedMap(new HashMap<String, AbstractSharedStorage>());
		storageList = Collections.synchronizedMap(new HashMap<String, List<AbstractSharedStorage>>());
		dirtyStorage = Collections.synchronizedList(new LinkedList<AbstractSharedStorage>());

		storageList.put("item", new ArrayList<AbstractSharedStorage>());
		storageList.put("liquid", new ArrayList<AbstractSharedStorage>());
		storageList.put("gas", new ArrayList<AbstractSharedStorage>());
		storageList.put("flux", new ArrayList<AbstractSharedStorage>());
		storageList.put("appeng", new ArrayList<AbstractSharedStorage>());
		storageList.put("bundledRedstone", new ArrayList<AbstractSharedStorage>());
		storageList.put("OpenComputers", new ArrayList<AbstractSharedStorage>());

		if (!client) {
			load();
		}
	}

	public void requestSave(AbstractSharedStorage storage) {
		dirtyStorage.add(storage);
	}

	public static SharedStorageHandler instance(boolean client) {
		return client ? clientStorageHandler : serverStorageHandler;
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
		storage.hoppingMode = hoppingMode;
		storage.setDirty();
	}

	public void setHoppingModeForAll(int coord, int side, int hoppingMode) {
		setHoppingMode(coord, side, "item", hoppingMode);
		setHoppingMode(coord, side, "liquid", hoppingMode);
		setHoppingMode(coord, side, "gas", hoppingMode);
		setHoppingMode(coord, side, "flux", hoppingMode);
		//setHoppingMode(coord, side, "appeng", hoppingMode);
		//setHoppingMode(coord, side, "bundledRedstone", hoppingMode);
	}

	public AbstractSharedStorage getStorage(int coord, int side, String type) {
		String key = coord + "|" + side + "|" + type;

		AbstractSharedStorage storage = storageMap.get(key);
		if (storage == null) {
			if (type.equals("item")) {
				storage = new ItemSharedStorage(this, coord, side);
			}

			if (type.equals("liquid")) {
				storage = new FluidSharedStorage(this, coord, side);
			}

			if (type.equals("gas")) {
				storage = new GasSharedStorage(this, coord, side);
			}

			if (type.equals("flux")) {
				storage = new FluxSharedStorage(this, coord, side);
			}

			if (type.equals("appeng")) {
				storage = new AESharedStorage(this, coord, side);
			}

			if (type.equals("bundledRedstone")) {
				storage = new BRSharedStorage(this, coord, side);
			}

			if (type.equals("OpenComputers")) {
				storage = new OpenComputersSharedStorage(this, coord, side);
			}

			if (!client && saveTag.hasKey(key)) {
				storage.loadFromTag(saveTag.getCompoundTag(key));
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
				saveTag.setTag(key, inv.saveToTag());
				inv.setClean();
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
