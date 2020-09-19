package com.robotgryphon.compactmachines.reference;

import net.minecraft.util.IStringSerializable;

public enum EnumMachineSize implements IStringSerializable {
    TINY    ("tiny", 4),
    SMALL   ("small", 6),
    NORMAL  ("normal", 8),
    LARGE   ("large", 10),
    GIANT   ("giant", 12),
    MAXIMUM ("maximum", 14);

    private String name;
    private int dimension;

    EnumMachineSize(String name, int dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public String getName() {
        return this.name;
    }

    public int getDimension() {
        return this.dimension;
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
    public String getString() {
        return this.name;
    }
}
