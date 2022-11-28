package dev.compactmods.machines.api.upgrade;

import dev.compactmods.machines.api.resource.IResourceType;
import dev.compactmods.machines.api.upgrade.requirement.IUpgradeRequirement;

import java.util.Collections;
import java.util.Set;

/**
 * An upgrade that deals with a resource in the room's resource networks.
 */
public interface IResourcefulUpgrade {

    IResourceType resourceType();

    default Set<IUpgradeRequirement> requirements() {
        return Collections.emptySet();
    }
}
