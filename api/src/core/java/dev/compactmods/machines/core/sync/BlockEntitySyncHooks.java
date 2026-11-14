package dev.compactmods.machines.core.sync;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Reports, on the server, when a block entity's data may have changed or been synced to clients, so
 * systems that mirror block entities elsewhere (the room preview) can react instead of polling. Fed by
 * mixins, because NeoForge has no event for it:
 * <ul>
 *     <li>{@link Listener#blockChanged} when a block entity is {@linkplain net.minecraft.world.level.block.entity.BlockEntity#setChanged() changed}
 *     or a block update is sent to clients (vanilla's block entity sync);</li>
 *     <li>{@link Listener#chunkSynced} when a mod sends its own packet to the players tracking a chunk
 *     (Mekanism's transporter updates, for example), which says where but not which block.</li>
 * </ul>
 * Called on hot paths, so listeners must return quickly when they don't care.
 */
public final class BlockEntitySyncHooks {

    public interface Listener {
        void blockChanged(ServerLevel level, BlockPos pos);

        void chunkSynced(ServerLevel level, ChunkPos chunk);
    }

    private static final Listener NONE = new Listener() {
        @Override
        public void blockChanged(ServerLevel level, BlockPos pos) {
        }

        @Override
        public void chunkSynced(ServerLevel level, ChunkPos chunk) {
        }
    };

    private static volatile Listener listener = NONE;

    private BlockEntitySyncHooks() {}

    /** Sets the one listener. Call during mod construction. */
    public static void setListener(Listener listener) {
        BlockEntitySyncHooks.listener = listener;
    }

    public static void blockChanged(ServerLevel level, BlockPos pos) {
        listener.blockChanged(level, pos);
    }

    public static void chunkSynced(ServerLevel level, ChunkPos chunk) {
        listener.chunkSynced(level, chunk);
    }
}
