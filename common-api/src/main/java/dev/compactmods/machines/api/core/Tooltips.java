package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

public interface Tooltips {

    ResourceLocation UNKNOWN_PLAYER_NAME = new ResourceLocation(Constants.MOD_ID, "unknown_player");
    ResourceLocation TUNNEL_TYPE = new ResourceLocation(Constants.MOD_ID, "tunnel_type");
    ResourceLocation UNKNOWN_TUNNEL_TYPE = new ResourceLocation(Constants.MOD_ID, "unknown_tunnel_type");
    ResourceLocation ROOM_NAME = new ResourceLocation(Constants.MOD_ID, "room_name");
    ResourceLocation ROOM_UPGRADE_TYPE = new ResourceLocation(Constants.MOD_ID, "room_upgrade_type");
    ResourceLocation TUTORIAL_APPLY_ROOM_UPGRADE = new ResourceLocation(Constants.MOD_ID, "tutorial_apply_room_upgrade");
    ResourceLocation CRAFT_TO_UPGRADE = new ResourceLocation(Constants.MOD_ID, "craft_machine_to_upgrade");
    ResourceLocation NOT_YET_IMPLEMENTED = new ResourceLocation(Constants.MOD_ID, "not_yet_implemented");

    interface Machines {
        ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "machine.id");
        ResourceLocation OWNER = new ResourceLocation(Constants.MOD_ID, "machine.owner");
        ResourceLocation SIZE = new ResourceLocation(Constants.MOD_ID, "machine.size");
        ResourceLocation BOUND_TO = new ResourceLocation(Constants.MOD_ID, "machine.bound_to");
    }

    //#region Hints and Details
    ResourceLocation HINT_HOLD_SHIFT = new ResourceLocation(Constants.MOD_ID, "hint.hold_shift");

    interface Details {
        ResourceLocation PERSONAL_SHRINKING_DEVICE = new ResourceLocation(Constants.MOD_ID, "details.psd");
        ResourceLocation SOLID_WALL = new ResourceLocation(Constants.MOD_ID, "details.solid_wall");
    }
    //#endregion
}
