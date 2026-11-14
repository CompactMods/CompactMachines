package dev.compactmods.machines.core.data.manager;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.data.DataFileUtil;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Intermediate, fluent builder for a {@link CodecFileManager.Single} — a single
 * codec-serialized file holding one {@code T} value.
 *
 * <p>Obtain one via {@link CodecFileManager#single(Codec)}, point it at a file with
 * {@link #at(Function)}, then materialize it against a running server with
 * {@link #build(MinecraftServer)}:
 *
 * <pre>{@code
 * CodecFileManager.Single<SpawnManager> manager =
 *     CodecFileManager.single(SpawnManager.CODEC)
 *         .at(CMRoomDataLocations.PLAYER_SPAWNS)
 *         .build(server);
 * }</pre>
 */
public final class SingleFileManagerBuilder<T> {

    private final Codec<T> valueCodec;

    private Function<MinecraftServer, Path> location;
    private Supplier<T> defaultValue = () -> null;

    SingleFileManagerBuilder(Codec<T> valueCodec) {
        this.valueCodec = valueCodec;
    }

    /**
     * Resolves the file this manager reads from and writes to. The function is applied
     * lazily against the server in {@link #build(MinecraftServer)}, so world-relative
     * locations (e.g. {@code CMRoomDataLocations.PLAYER_SPAWNS}) can be supplied directly.
     */
    public SingleFileManagerBuilder<T> at(Function<MinecraftServer, Path> location) {
        this.location = location;
        return this;
    }

    /**
     * Value used when the backing file does not yet exist. Defaults to {@code null}
     * (the manager's accessor is nullable).
     */
    public SingleFileManagerBuilder<T> withDefault(Supplier<T> defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        return this;
    }

    public CodecFileManager.Single<T> build(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(location, "location not set — call at(...) before build(...)");

        final var file = location.apply(server);
        final var parent = file.getParent();
        if (parent != null)
            DataFileUtil.ensureDirExists(parent);

        return new CodecFileManager.Single<>(server, valueCodec, file, defaultValue);
    }
}
