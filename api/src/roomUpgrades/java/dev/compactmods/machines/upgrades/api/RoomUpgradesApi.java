package dev.compactmods.machines.upgrades.api;

import net.neoforged.neoforge.registries.DeferredRegister;

public interface RoomUpgradesApi {

    static DeferredRegister<RoomUpgradeComponentType<?>> roomUpgradeDR(String namespace) {
        return DeferredRegister.create(RoomUpgradeComponentType.REGISTRY_KEY, namespace);
    }

}
