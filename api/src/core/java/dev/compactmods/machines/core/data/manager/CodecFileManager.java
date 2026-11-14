package dev.compactmods.machines.core.data.manager;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.data.DataFileUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.file.PathUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract sealed class CodecFileManager implements AutoCloseable {

    protected final MinecraftServer server;

    protected CodecFileManager(MinecraftServer server) {
        this.server = server;
    }

    public abstract void save();

    @Override
    public void close() {
        save();
    }

    /**
     * Begins building a directory-backed, per-key file store. Each key is serialized to its
     * own {@code .dat} file named via {@code keyToFilename}.
     *
     * @param keyToFilename maps a key to its file's base name (extension is added automatically)
     * @param valueCodec    codec used to (de)serialize each value
     */
    public static <Key, Value> KeyedFileManagerBuilder<Key, Value> keyed(Function<Key, String> keyToFilename, Codec<Value> valueCodec) {
        return new KeyedFileManagerBuilder<>(keyToFilename, valueCodec);
    }

    /**
     * Begins building a single-file store holding one value.
     *
     * @param valueCodec codec used to (de)serialize the value
     */
    public static <T> SingleFileManagerBuilder<T> single(Codec<T> valueCodec) {
        return new SingleFileManagerBuilder<>(valueCodec);
    }

    public static final class Single<T> extends CodecFileManager {
        private final Path filename;
        private final Codec<T> valueCodec;
        private T data;

        Single(MinecraftServer server, Codec<T> valueCodec, Path filename, Supplier<T> defaultValue) {
            super(server);
            this.filename = filename;
            this.valueCodec = valueCodec;
            this.data = !filename.toFile().exists() ?
                    defaultValue.get() :
                    DataFileUtil.loadFileWithCodec(filename.toFile(), valueCodec);
        }

        @Nullable
        public T data() {
            return data;
        }

        public Optional<T> optionalData() {
            return filename.toFile().exists() ? Optional.ofNullable(data) : Optional.empty();
        }

        public void set(@NonNull T data) {
            this.data = data;
            save();
        }

        @Override
        public void save() {
            DataFileUtil.save(filename, valueCodec, data);
        }
    }

    public static final class Keyed<Key, Value> extends CodecFileManager {

        private final HashMap<Key, Value> cache;
        private final Set<Key> dirtyFiles;
        private final Codec<Value> valueCodec;
        private final Supplier<Value> defaultValue;
        private final Path directory;
        private final Function<Key, String> keyToFilename;

        Keyed(MinecraftServer server, Codec<Value> valueCodec, Supplier<Value> defaultValue, Path directory, Function<Key, String> keyToFilename) {
            super(server);
            this.valueCodec = valueCodec;
            this.defaultValue = defaultValue;
            this.directory = directory;
            this.keyToFilename = keyToFilename;
            this.dirtyFiles = new ObjectOpenHashSet<>();
            this.cache = new HashMap<>();
        }

        @Nullable
        public Value data(Key key) {
            return cache.computeIfAbsent(key, k -> {
                final var filename = keyToFilename.apply(k) + ".dat";
                final var file = directory.resolve(filename).toFile();
                return !file.exists() ? defaultValue.get() : DataFileUtil.loadFileWithCodec(file, valueCodec);
            });
        }

        public Optional<Value> optionalData(Key key) {
            return hasData(key) ? Optional.ofNullable(data(key)) : Optional.empty();
        }

        public void set(Key key, @NonNull Value data) {
            cache.put(key, data);
            this.dirtyFiles.add(key);
        }

        public void markDirty(Key key) {
            this.dirtyFiles.add(key);
        }

        public Stream<String> existingFiles() {
            // try-with-resources so the directory stream (a file descriptor) is always closed — a lazy
            // Files.list(...) returned unclosed leaks an fd on every call, and this is hit every tick.
            try (var list = Files.list(directory)) {
                return list.filter(Files::isRegularFile)
                        .map(PathUtils::getBaseName)
                        .toList()
                        .stream();
            } catch (IOException e) {
                return Stream.empty();
            }
        }

        public boolean hasData(Key key) {
            return cache.containsKey(key) || DataFileUtil.hasFile(directory, keyToFilename.apply(key));
        }

        public void save() {
            dirtyFiles.forEach((key) -> {
                final var data = this.cache.get(key);
                final var filename = DataFileUtil.getDataFile(directory, keyToFilename.apply(key));
                DataFileUtil.save(filename, valueCodec, data);
            });

            dirtyFiles.clear();
        }
    }
}
