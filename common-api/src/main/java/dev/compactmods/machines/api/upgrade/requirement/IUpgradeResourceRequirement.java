package dev.compactmods.machines.api.upgrade.requirement;

import dev.compactmods.machines.api.resource.IResourceType;

public interface IUpgradeResourceRequirement extends IUpgradeRequirement {

    IResourceType resourceType();
}
