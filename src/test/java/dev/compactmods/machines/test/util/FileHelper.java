package dev.compactmods.machines.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

public final class FileHelper {
    public static final FileHelper INSTANCE = new FileHelper();
    public static Path RESOURCES_DIR = Paths.get("src","test","resources");

    private FileHelper() {
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
        return NbtIo.readCompressed(isr);
    }
}
