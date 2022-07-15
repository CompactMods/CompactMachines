package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

public abstract class Tooltips {

    public static final ResourceLocation UNKNOWN_PLAYER_NAME = new ResourceLocation(Constants.MOD_ID, "unknown_player");
    public static final ResourceLocation TUNNEL_TYPE = new ResourceLocation(Constants.MOD_ID, "tunnel_type");
    public static final ResourceLocation UNKNOWN_TUNNEL_TYPE = new ResourceLocation(Constants.MOD_ID, "unknown_tunnel_type");
    public static final ResourceLocation ROOM_NAME = new ResourceLocation(Constants.MOD_ID, "room_name");
    public static final ResourceLocation ROOM_UPGRADE_TYPE = new ResourceLocation(Constants.MOD_ID, "room_upgrade_type");
    public static final ResourceLocation TUTORIAL_APPLY_ROOM_UPGRADE = new ResourceLocation(Constants.MOD_ID, "tutorial_apply_room_upgrade");

    public static abstract class Machines {
        public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "machine.id");
        public static final ResourceLocation OWNER = new ResourceLocation(Constants.MOD_ID, "machine.owner");
        public static final ResourceLocation SIZE = new ResourceLocation(Constants.MOD_ID, "machine.size");
        public static final ResourceLocation BOUND_TO = new ResourceLocation(Constants.MOD_ID, "machine.bound_to");
    }

    //#region Hints and Details
    public static final ResourceLocation HINT_HOLD_SHIFT = new ResourceLocation(Constants.MOD_ID, "hint.hold_shift");

    public static abstract class Details {
        public static final ResourceLocation PERSONAL_SHRINKING_DEVICE = new ResourceLocation(Constants.MOD_ID, "details.psd");
        public static final ResourceLocation SOLID_WALL = new ResourceLocation(Constants.MOD_ID, "details.solid_wall");
    }
    //#endregion
}
