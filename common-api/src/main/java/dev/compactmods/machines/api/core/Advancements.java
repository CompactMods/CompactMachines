package dev.compactmods.machines.api.core;

import net.minecraft.resources.ResourceLocation;

public interface Advancements {
    ResourceLocation HOW_DID_YOU_GET_HERE = new ResourceLocation(Constants.MOD_ID, "how_did_you_get_here");
    ResourceLocation ROOT = new ResourceLocation(Constants.MOD_ID, "root");
    ResourceLocation FOUNDATIONS = new ResourceLocation(Constants.MOD_ID, "foundations");

    ResourceLocation GOT_SHRINKING_DEVICE = new ResourceLocation(Constants.MOD_ID, "got_shrinking_device");

    ResourceLocation CLAIMED_TINY_MACHINE = new ResourceLocation(Constants.MOD_ID, "claimed_machine_tiny");
    ResourceLocation CLAIMED_SMALL_MACHINE = new ResourceLocation(Constants.MOD_ID, "claimed_machine_small");
    ResourceLocation CLAIMED_NORMAL_MACHINE = new ResourceLocation(Constants.MOD_ID, "claimed_machine_normal");
    ResourceLocation CLAIMED_LARGE_MACHINE = new ResourceLocation(Constants.MOD_ID, "claimed_machine_large");
    ResourceLocation CLAIMED_GIANT_MACHINE = new ResourceLocation(Constants.MOD_ID, "claimed_machine_giant");
    ResourceLocation CLAIMED_MAX_MACHINE = new ResourceLocation(Constants.MOD_ID, "claimed_machine_max");

    ResourceLocation RECURSIVE_ROOMS = new ResourceLocation(Constants.MOD_ID, "recursion");
}
