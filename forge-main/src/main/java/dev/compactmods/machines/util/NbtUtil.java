package dev.compactmods.machines.util;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.codec.CodecExtensions;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

public class NbtUtil {

    public static ChunkPos readChunkPos(Tag tag) {
        return CodecExtensions.CHUNKPOS.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, CompactMachines.LOGGER::fatal);
    }
}
