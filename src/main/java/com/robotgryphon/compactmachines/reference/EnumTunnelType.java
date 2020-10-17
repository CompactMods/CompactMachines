package com.robotgryphon.compactmachines.reference;

import net.minecraft.util.IStringSerializable;

public enum EnumTunnelType implements IStringSerializable {
    ITEM("item"),
    REDSTONE_IN("redstone_in"),
    REDSTONE_OUT("redstone_out");

    private String type;

    EnumTunnelType(String typeName) {
        this.type = typeName;
    }

    @Override
    public String getString() {
        return type;
    }
}
