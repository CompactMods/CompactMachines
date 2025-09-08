package dev.compactmods.machines.util.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface VanillaCodecs {

    StreamCodec<ByteBuf, StructureTemplate> STRUCTURE_TEMPLATE_STREAM_CODEC = ByteBufCodecs.TRUSTED_COMPOUND_TAG
            .map(nbt -> {
                final var struct = new StructureTemplate();
                struct.load(BuiltInRegistries.BLOCK, nbt);
                return struct;
            }, template -> template.save(new CompoundTag()));
}
