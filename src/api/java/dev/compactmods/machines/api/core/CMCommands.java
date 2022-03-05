package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class CMCommands {
    public static final ResourceLocation CMD_DIM_REGISTERED = new ResourceLocation(MOD_ID, "level_registered");
    public static final ResourceLocation CMD_DIM_NOT_FOUND = new ResourceLocation(MOD_ID, "level_not_found");

    /**
     * Used for displaying the number of registered machines via summary commands.
     */
    public static final ResourceLocation MACHINE_REG_COUNT = new ResourceLocation(MOD_ID, "machine_reg_count");

    /**
     * Used for displaying the number of registered rooms via summary commands.
     */
    public static final ResourceLocation ROOM_REG_COUNT = new ResourceLocation(MOD_ID, "room_reg_count");
}
