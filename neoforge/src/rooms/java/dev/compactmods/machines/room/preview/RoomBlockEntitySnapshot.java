package dev.compactmods.machines.room.preview;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * One block entity in a {@link RoomPreviewSnapshot}: its type and the same sync tag vanilla sends
 * clients when a chunk loads ({@link BlockEntity#getUpdateTag}). The client rebuilds the block entity
 * from it, so models that read block entity data (pipe connections, for example) and block entity
 * renderers (chests) show as they do in the world.
 *
 * @param local Room-local position, as the snapshot's cells.
 */
public record RoomBlockEntitySnapshot(BlockPos local, BlockEntityType<?> type, CompoundTag tag) {

    public static final StreamCodec<ByteBuf, RoomBlockEntitySnapshot> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RoomBlockEntitySnapshot::local,
            ByteBufCodecs.idMapper(BuiltInRegistries.BLOCK_ENTITY_TYPE), RoomBlockEntitySnapshot::type,
            ByteBufCodecs.COMPOUND_TAG, RoomBlockEntitySnapshot::tag,
            RoomBlockEntitySnapshot::new);
}
