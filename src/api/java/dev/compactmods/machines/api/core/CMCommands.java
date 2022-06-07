package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class CMCommands {
    public static final ResourceLocation LEVEL_REGISTERED = new ResourceLocation(MOD_ID, "level_registered");
    public static final ResourceLocation LEVEL_NOT_FOUND = new ResourceLocation(MOD_ID, "level_not_found");

    public static final ResourceLocation ROOM_NOT_FOUND = new ResourceLocation(MOD_ID, "room_not_found");

    /**
     * Used for displaying the number of registered machines via summary commands.
     */
    public static final ResourceLocation MACHINE_REG_DIM = new ResourceLocation(MOD_ID, "summary.machines.dimension");
    public static final ResourceLocation MACHINE_REG_TOTAL = new ResourceLocation(MOD_ID, "summary.machines.total");

    /**
     * Used for displaying the number of registered rooms via summary commands.
     */
    public static final ResourceLocation ROOM_REG_COUNT = new ResourceLocation(MOD_ID, "room_reg_count");
    public static final ResourceLocation NOT_A_MACHINE_BLOCK = new ResourceLocation(MOD_ID, "not_a_machine_block");

    /**
     * Shows a machine is not bound. Takes in a single param, the machine position in world.
     */
    public static final ResourceLocation MACHINE_NOT_BOUND = new ResourceLocation(MOD_ID, "machine_not_bound");

    public static final ResourceLocation WRONG_DIMENSION = new ResourceLocation(MOD_ID, "not_in_compact_dimension");
    public static final ResourceLocation NOT_IN_COMPACT_DIMENSION = new ResourceLocation(MOD_ID, "not_in_compact_dim");
    public static final ResourceLocation FAILED_CMD_FILE_ERROR = new ResourceLocation(MOD_ID, "failed_command_file_error");

    public static final ResourceLocation CANNOT_GIVE_MACHINE = new ResourceLocation(MOD_ID, "cannot_give_machine_item");
    public static final ResourceLocation MACHINE_GIVEN = new ResourceLocation(MOD_ID, "machine_given_successfully");
    public static final ResourceLocation NO_REBIND_TUNNEL_PRESENT = new ResourceLocation(MOD_ID, "cannot_rebind_tunnel_present");
    public static final ResourceLocation SPAWN_CHANGED_SUCCESSFULLY = new ResourceLocation(MOD_ID, "spawn_changed_successfully");
}
