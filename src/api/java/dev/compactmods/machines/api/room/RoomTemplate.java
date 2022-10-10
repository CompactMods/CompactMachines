package dev.compactmods.machines.api.room;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.core.Constants;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Template structure for creating a new Compact Machine room. These can be added and removed from the registry
 * at any point, so persistent data must be stored outside these instances.
 *
 * @param dimensions The internal dimensions of the room when it is created.
 * @param color The color of the machine blocks created for this template.
 * @param prefillTemplate A template (structure) file reference, if specified this will fill the new room post-generation
 */
public record RoomTemplate(Vec3i dimensions, int color, ResourceLocation prefillTemplate) {

    public static final ResourceLocation NO_TEMPLATE = new ResourceLocation(Constants.MOD_ID, "empty");
    public static final RoomTemplate INVALID_TEMPLATE = new RoomTemplate(0, 0);

    public static Codec<RoomTemplate> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3i.CODEC.fieldOf("dimensions").forGetter(RoomTemplate::dimensions),
            Codec.INT.fieldOf("color").forGetter(RoomTemplate::color),
            ResourceLocation.CODEC.optionalFieldOf("template", NO_TEMPLATE).forGetter(RoomTemplate::prefillTemplate)
    ).apply(i, RoomTemplate::new));

    public RoomTemplate(int cubicSize, int color) {
        this(new Vec3i(cubicSize, cubicSize, cubicSize), color, NO_TEMPLATE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomTemplate that = (RoomTemplate) o;
        return color == that.color && Objects.equals(dimensions, that.dimensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensions, color);
    }
}
