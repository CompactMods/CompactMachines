package dev.compactmods.machines.api.room.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

public record RoomStructureInfo(Identifier template, RoomStructurePlacement placement) {
    public static final Codec<RoomStructureInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("template").forGetter(RoomStructureInfo::template),
            RoomStructurePlacement.CODEC.fieldOf("placement").forGetter(RoomStructureInfo::placement)
    ).apply(inst, RoomStructureInfo::new));

    public static final StreamCodec<FriendlyByteBuf, RoomStructureInfo> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, RoomStructureInfo::template,
            NeoForgeStreamCodecs.enumCodec(RoomStructurePlacement.class), RoomStructureInfo::placement,
            RoomStructureInfo::new
    );

    public enum RoomStructurePlacement implements StringRepresentable {
        CENTERED_CEILING("centered_ceiling"),
        CENTERED("centered"),
        CENTERED_FLOOR("centered_floor");

        public static final StringRepresentable.StringRepresentableCodec<RoomStructurePlacement> CODEC = StringRepresentable.fromEnum(RoomStructurePlacement::values);

        private final String name;

        RoomStructurePlacement(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
