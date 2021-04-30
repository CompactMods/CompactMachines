package com.robotgryphon.compactmachines.tests.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class FileHelper {
    public static final FileHelper INSTANCE = new FileHelper();

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

    public JsonElement getJsonFromFile(String filename) {
        Gson g = new Gson();
        InputStreamReader isr = openFile(filename);
        return g.fromJson(isr, JsonElement.class);
    }

    public CompoundNBT getNbtFromFile(String filename) throws IOException {
        InputStream isr = getFileStream(filename);
        return CompressedStreamTools.readCompressed(isr);
    }
}
