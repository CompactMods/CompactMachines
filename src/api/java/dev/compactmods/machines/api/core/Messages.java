package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

public abstract class Messages {
    public static final ResourceLocation CANNOT_ENTER_MACHINE = new ResourceLocation(Constants.MOD_ID, "cannot_enter");
    public static final ResourceLocation NO_MACHINE_DATA = new ResourceLocation(Constants.MOD_ID, "no_machine_data");
    public static final ResourceLocation ROOM_SPAWNPOINT_SET = new ResourceLocation(Constants.MOD_ID, "spawnpoint_set");
    public static final ResourceLocation TELEPORT_OUT_OF_BOUNDS = new ResourceLocation(Constants.MOD_ID, "teleport_oob");
    public static final ResourceLocation HOW_DID_YOU_GET_HERE = new ResourceLocation(Constants.MOD_ID, "how_did_you_get_here");
    public static final ResourceLocation UNKNOWN_TUNNEL = new ResourceLocation(Constants.MOD_ID, "unknown_tunnel_type");
    public static final ResourceLocation NO_TUNNEL_SIDE = new ResourceLocation(Constants.MOD_ID, "no_available_sides");
    public static final ResourceLocation UNKNOWN_ROOM_CHUNK = new ResourceLocation(Constants.MOD_ID, "unknown_room_chunk");
    public static final ResourceLocation UNREGISTERED_CM_DIM = new ResourceLocation(Constants.MOD_ID, "dimension_not_registered");
    public static final ResourceLocation NEW_MACHINE = new ResourceLocation(Constants.MOD_ID, "new_machine");

    /**
     * Used to show information about a player inside a Compact room.
     */
    public static final ResourceLocation PLAYER_ROOM_INFO = new ResourceLocation(Constants.MOD_ID, "player_room_info");

    /**
     * Used to show information about a room, accessed via a bound machine.
     */
    public static final ResourceLocation MACHINE_ROOM_INFO = new ResourceLocation(Constants.MOD_ID, "machine_room_info");

    /**
     * Shown when a non-owner tries to rename a room. Takes the owner's display name.
     */
    public static final ResourceLocation CANNOT_RENAME_NOT_OWNER = new ResourceLocation(Constants.MOD_ID, "cannot_rename_not_owner");
}
