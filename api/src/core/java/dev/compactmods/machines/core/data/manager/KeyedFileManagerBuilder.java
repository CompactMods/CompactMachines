package dev.compactmods.machines.core.data.manager;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.data.DataFileUtil;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Intermediate, fluent builder for a {@link CodecFileManager.Keyed} — a directory-backed
 * store where each {@code Key} maps to its own codec-serialized {@code .dat} file.
 *
 * <p>Obtain one via {@link CodecFileManager#keyed(Function, Codec)}, point it at a
 * directory with {@link #at(Function)}, then materialize it against a running server
 * with {@link #build(MinecraftServer)}:
 *
 * <pre>{@code
 * CodecFileManager.Keyed<String, RoomInstanceData> registry =
 *     CodecFileManager.keyed(Function.identity(), RoomInstanceData.CODEC)
 *         .at(CMRoomDataLocations.REGISTRY_FILES)
 *         .build(server);
 * }</pre>
 */
public final class KeyedFileManagerBuilder<Key, Value> {

    private final Function<Key, String> keyToFilename;
    private final Codec<Value> valueCodec;

    private Function<MinecraftServer, Path> directory;
    private Supplier<Value> defaultValue = () -> null;

    KeyedFileManagerBuilder(Function<Key, String> keyToFilename, Codec<Value> valueCodec) {
        this.keyToFilename = keyToFilename;
        this.valueCodec = valueCodec;
    }

    /**
     * Resolves the directory that backing files live in. The function is applied lazily
     * against the server in {@link #build(MinecraftServer)}, so world-relative locations
     * (e.g. {@code CMRoomDataLocations.REGISTRY_FILES}) can be supplied directly.
     */
    public KeyedFileManagerBuilder<Key, Value> at(Function<MinecraftServer, Path> directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Value returned by {@link CodecFileManager.Keyed#data(Object)} when no file exists yet
     * for a key. Defaults to {@code null} (the manager's accessors are nullable).
     */
    public KeyedFileManagerBuilder<Key, Value> withDefault(Supplier<Value> defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        return this;
    }

    public CodecFileManager.Keyed<Key, Value> build(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(directory, "directory not set — call at(...) before build(...)");

        final var dir = directory.apply(server);
        DataFileUtil.ensureDirExists(dir);

        return new CodecFileManager.Keyed<>(server, valueCodec, defaultValue, dir, keyToFilename);
    }
}
