package dev.compactmods.machines.reference;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

public enum EnumMachineSize implements StringRepresentable {
    TINY    ("tiny", 3),
    SMALL   ("small", 5),
    NORMAL  ("normal", 7),
    LARGE   ("large", 9),
    GIANT   ("giant", 11),
    MAXIMUM ("maximum", 13);

    private final String name;
    private final int internalSize;

    public static final Codec<EnumMachineSize> CODEC = StringRepresentable.fromEnum(
            EnumMachineSize::values, EnumMachineSize::getFromSize);

    EnumMachineSize(String name, int internalSize) {
        this.name = name;
        this.internalSize = internalSize;
    }

    public static EnumMachineSize maximum() {
        return MAXIMUM;
    }

    public String getName() {
        return this.name;
    }

    public AABB getBounds(BlockPos center) {
        AABB bounds = new AABB(center);
        return bounds.inflate(Math.floorDiv(internalSize, 2));
    }

    public int getInternalSize() {
        return this.internalSize;
    }

    public static EnumMachineSize getFromSize(String size) {
        switch (size.toLowerCase()) {
            case "tiny": return TINY;
            case "small": return SMALL;
            case "normal": return NORMAL;
            case "large": return LARGE;
            case "giant": return GIANT;
            case "maximum": return MAXIMUM;
        }

        return TINY;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
