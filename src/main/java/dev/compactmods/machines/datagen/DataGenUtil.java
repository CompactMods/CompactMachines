package dev.compactmods.machines.datagen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.compactmods.machines.CompactMachines;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

class DataGenUtil {
    static <T> BiConsumer<T, ResourceLocation> makeWriter(Gson gson, @NotNull HashCache cache, Path dataDir, ImmutableSet<String> pathParts, Codec<T> codec, HashMap<ResourceLocation, T> set) {
        return (T resource, ResourceLocation regName) -> {
            if (set.containsKey(regName)) {
                throw new IllegalStateException("Duplicate resource " + regName);
            } else {
                String namespace = regName.getNamespace();
                String path = regName.getPath();

                Path fileLocation = dataDir.resolve(Path.of("data", CompactMachines.MOD_ID));
                for (String p : pathParts)
                    fileLocation = fileLocation.resolve(p);

                fileLocation = fileLocation.resolve(path + ".json");

                try {
                    //noinspection OptionalGetWithoutIsPresent
                    DataProvider.save(gson, cache, codec.encodeStart(JsonOps.INSTANCE, resource).result().get(), fileLocation);

                    set.put(regName, resource);
                } catch (IOException ioe) {
                    CompactMachines.LOGGER.error("Couldn't save resource {}", fileLocation, ioe);
                }
            }
        };
    }

    static <T> BiConsumer<T, ResourceLocation> makeCustomWriter(Gson gson, @NotNull HashCache cache, Path dataDir, ImmutableSet<String> pathParts, Function<T, JsonElement> writer, HashMap<ResourceLocation, T> set) {
        return (T resource, ResourceLocation regName) -> {
            if (set.containsKey(regName)) {
                throw new IllegalStateException("Duplicate resource " + regName);
            } else {
                String namespace = regName.getNamespace();
                String path = regName.getPath();

                Path fileLocation = dataDir.resolve(Path.of("data", CompactMachines.MOD_ID));
                for (String p : pathParts)
                    fileLocation = fileLocation.resolve(p);

                fileLocation = fileLocation.resolve(path + ".json");

                try {
                    DataProvider.save(gson, cache, writer.apply(resource), fileLocation);

                    set.put(regName, resource);
                } catch (IOException ioe) {
                    CompactMachines.LOGGER.error("Couldn't save resource {}", fileLocation, ioe);
                }
            }
        };
    }
}
