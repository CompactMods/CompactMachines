package dev.compactmods.machines.data;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.common.IOUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataFileUtil {

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
