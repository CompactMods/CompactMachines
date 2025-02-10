package dev.compactmods.machines.data.manager;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.data.CMDataFile;
import dev.compactmods.machines.data.CodecHolder;
import dev.compactmods.machines.data.DataFileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.IOUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A codec-backed file that stores several instances of typed data, indexed by a key.
 *
 * @param <Key> The key used for instance lookups.
 * @param <T>
 */
public class CMKeyedDataFileManager<Key, T extends CMDataFile & CodecHolder<T>> implements IKeyedDataFileManager<Key, T> {

   protected final MinecraftServer server;
   private final BiFunction<MinecraftServer, Key, T> creator;
   private final HashMap<Key, T> cache;

   public CMKeyedDataFileManager(MinecraftServer server, BiFunction<MinecraftServer, Key, T> creator) {
	  this.server = server;
	  this.creator = creator;
	  this.cache = new HashMap<>();
   }

	/**
	 * Used to get the filename for a given item, if {@code key.toString()} is not sufficient.
	 *
	 * @param key
	 * @return
	 */
   public String getFileKey(Key key) {
	   return key.toString();
   }

   @Override
   public T data(Key key) {
	  return cache.computeIfAbsent(key, k -> {
		 var inst = creator.apply(server, k);
		 var dir = inst.getDataLocation(server);
		 DataFileUtil.ensureDirExists(dir);
		 final var file = dir.resolve(getFileKey(k) + ".dat").toFile();
		 return !file.exists() ? inst : DataFileUtil.loadFileWithCodec(file, inst.codec());
	  });
   }

   @Override
   public Optional<T> optionalData(Key key) {
	  return hasData(key) ? Optional.ofNullable(data(key)) : Optional.empty();
   }

   public void save() {
	  cache.forEach((key, data) -> {
		 var fullData = new CompoundTag();
		 fullData.putString("version", data.getDataVersion());

		 var fileData = data.codec()
			 .encodeStart(NbtOps.INSTANCE, data)
			 .getOrThrow();

		 fullData.put("data", fileData);

		 try {
			 IOUtilities.writeNbtCompressed(fullData, data.getDataLocation(server).resolve(getFileKey(key) + ".dat"));
		 } catch (IOException e) {
			LoggingUtil.modLog().error("Failed to write data: " + e.getMessage(), e);
		 }
	  });
   }

   public boolean hasData(Key key) {
	  return cache.containsKey(key);
   }
}
