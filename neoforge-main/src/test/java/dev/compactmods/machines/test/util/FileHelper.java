package dev.compactmods.machines.test.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;

public final class FileHelper {
    public static final FileHelper INSTANCE = new FileHelper();

    private FileHelper() {
    }

    @Nullable
    public static Path resourcesDir() {
        final var rd = System.getenv("CM5_TEST_RESOURCES");
        return rd == null ? null : Path.of(rd);
    }

    public InputStream getFileStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    public InputStreamReader openFile(String filename) {
        URL res = getClass().getClassLoader().getResource(filename);
        try {
            InputStream inputStream = res.openStream();
            return new InputStreamReader(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JsonElement getJsonFromFile(String filename) {
        Gson g = new Gson();
        InputStreamReader isr = INSTANCE.openFile(filename);
        return g.fromJson(isr, JsonElement.class);
    }

    public static CompoundTag getNbtFromFile(String filename) throws IOException {
        InputStream isr = INSTANCE.getFileStream(filename);
        return NbtIo.readCompressed(isr, NbtAccounter.unlimitedHeap());
    }

    public static CompoundTag getNbtFromSavedDataFile(String filename) throws IOException {
        InputStream isr = INSTANCE.getFileStream(filename);
        final var nbtRoot = NbtIo.readCompressed(isr, NbtAccounter.unlimitedHeap());
        return nbtRoot.getCompound("data");
    }

    public static <T extends Tag> T getNbtFromSavedDataFile(String filename, Class<T> tagClass) throws IOException {
        InputStream isr = INSTANCE.getFileStream(filename);
        final var nbtRoot = NbtIo.readCompressed(isr, NbtAccounter.unlimitedHeap());
        final var tag = nbtRoot.get("data");
        if(tagClass.isInstance(tag))
            return tagClass.cast(tag);

        return null;
    }
}
