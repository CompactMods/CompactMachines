package dev.compactmods.machines.core.data;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.neoforge.common.IOUtilities;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataFileUtil {

	public static Path getDataFile(Path directory, String baseName) {
		DataFileUtil.ensureDirExists(directory);
		return directory.resolve(baseName + ".dat");
	}

	public static boolean hasFile(Path directory, String baseName) {
		return getDataFile(directory, baseName).toFile().exists();
	}

	public static <T> void save(Path filename, Codec<T> codec, T data) {
		var fullData = new CompoundTag();
		fullData.putString("version", "1.0.0");
		fullData.store("data", codec, data);

		try {
			IOUtilities.writeNbtCompressed(fullData, filename);
		} catch (IOException e) {
			var logger = LogManager.getLogger();
			logger.error("Failed to write data: {}", e.getMessage(), e);
		}
	}

	public static void ensureDirExists(Path dir) {
		if (!Files.exists(dir)) {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static <T> T loadFileWithCodec(File file, Codec<T> codec) {
		try {
			IOUtilities.tryCleanupTempFiles(Path.of(file.getParent()), file.getName());
			try (var is = new FileInputStream(file)) {
				final var tag = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
				return tag.read("data", codec).orElseThrow();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
