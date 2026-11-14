package dev.compactmods.machines;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.resources.Identifier;

/**
 * Holds references to advancements and grantables.
 */
public interface Advancements {
    /**
     * Granted when the player is teleported out by leaving room boundaries, or
     * when in an invalid state (such as trying to leave a machine room with no entry history)
     */
    Identifier HOW_DID_YOU_GET_HERE = CompactMachinesCore.identifier("how_did_you_get_here");

    /**
     * Root advancement. Required for vanilla's tree design.
     */
    Identifier ROOT = CompactMachinesCore.identifier("root");

    /**
     * Granted when a player first crafts machine items.
     */
    Identifier FOUNDATIONS = CompactMachinesCore.identifier("foundations");

    /**
     * Granted on first pickup of a PSD item.
     */
    Identifier GOT_SHRINKING_DEVICE = CompactMachinesCore.identifier("got_shrinking_device");

    /**
     * Granted if a player tries to enter a machine room they're currently in.
     */
    Identifier RECURSIVE_ROOMS = CompactMachinesCore.identifier("recursion");
}
