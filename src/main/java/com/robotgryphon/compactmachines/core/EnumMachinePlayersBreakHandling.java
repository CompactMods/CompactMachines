package com.robotgryphon.compactmachines.core;

public enum EnumMachinePlayersBreakHandling {
    UNBREAKABLE("unbreakable", "Nobody can break while players are inside."),
    OWNER("owner", "Only the owner can break while players are inside."),
    ANYONE("anyone", "Anyone can break while players are inside.");

    private final String configValue;
    private final String configDesc;

    EnumMachinePlayersBreakHandling(String configValue, String configDesc) {
        this.configValue = configValue;
        this.configDesc = configDesc;
    }

    public String configName() {
        return configValue;
    }

    public String configDesc() {
        return configDesc;
    }
}
