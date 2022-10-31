package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

public interface Messages {
    ResourceLocation CANNOT_ENTER_MACHINE = new ResourceLocation(Constants.MOD_ID, "cannot_enter");
    ResourceLocation NO_MACHINE_DATA = new ResourceLocation(Constants.MOD_ID, "no_machine_data");
    ResourceLocation ROOM_SPAWNPOINT_SET = new ResourceLocation(Constants.MOD_ID, "spawnpoint_set");
    ResourceLocation TELEPORT_OUT_OF_BOUNDS = new ResourceLocation(Constants.MOD_ID, "teleport_oob");
    ResourceLocation HOW_DID_YOU_GET_HERE = new ResourceLocation(Constants.MOD_ID, "how_did_you_get_here");
    ResourceLocation UNKNOWN_TUNNEL = new ResourceLocation(Constants.MOD_ID, "unknown_tunnel_type");
    ResourceLocation NO_TUNNEL_SIDE = new ResourceLocation(Constants.MOD_ID, "no_available_sides");
    ResourceLocation UNKNOWN_ROOM_CHUNK = new ResourceLocation(Constants.MOD_ID, "unknown_room_chunk");
    ResourceLocation UNREGISTERED_CM_DIM = new ResourceLocation(Constants.MOD_ID, "dimension_not_registered");
    ResourceLocation NEW_MACHINE = new ResourceLocation(Constants.MOD_ID, "new_machine");

    /**
     * Used to show information about a player inside a Compact room.
     */
    ResourceLocation PLAYER_ROOM_INFO = new ResourceLocation(Constants.MOD_ID, "player_room_info");

    /**
     * Used to show information about a room, accessed via a bound machine.
     */
    ResourceLocation MACHINE_ROOM_INFO = new ResourceLocation(Constants.MOD_ID, "machine_room_info");

    /**
     * Shown when a non-owner tries to rename a room. Takes the owner's display name.
     */
    ResourceLocation CANNOT_RENAME_NOT_OWNER = new ResourceLocation(Constants.MOD_ID, "cannot_rename_not_owner");

    /**
     * Shown to players when they try to interact with a room they do not own. Provides the owner's display name.
     */
    ResourceLocation NOT_ROOM_OWNER = new ResourceLocation(Constants.MOD_ID, "not_the_room_owner");
    ResourceLocation UPGRADE_APPLIED = new ResourceLocation(Constants.MOD_ID, "upgrade_applied");
    ResourceLocation UPGRADE_ADD_FAILED = new ResourceLocation(Constants.MOD_ID, "upgrade_add_failed");
    ResourceLocation UPGRADE_REMOVED = new ResourceLocation(Constants.MOD_ID, "upgrade_removed");
    ResourceLocation UPGRADE_REM_FAILED = new ResourceLocation(Constants.MOD_ID, "upgrade_remove_failed");
    ResourceLocation ALREADY_HAS_UPGRADE = new ResourceLocation(Constants.MOD_ID, "upgrade_already_present");
    ResourceLocation UPGRADE_NOT_PRESENT = new ResourceLocation(Constants.MOD_ID, "upgrade_not_present");
}
