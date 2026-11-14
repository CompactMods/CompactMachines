package dev.compactmods.machines.upgrades.api;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

import java.util.function.Supplier;

public record RoomUpgradeComponentType<T extends RoomUpgradeComponent>(
        Supplier<T> constructor,
        FeatureFlagSet requiredFeatures
) implements FeatureElement {

    public static final ResourceKey<Registry<RoomUpgradeComponentType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(CompactMachinesCore.identifier("room_upgrade_component"));

    public static <T extends RoomUpgradeComponent> Builder<T> builder(Supplier<T> constructor) {
        return new Builder<>(constructor);
    }

    public static class Builder<T extends RoomUpgradeComponent> {
        private final Supplier<T> constructor;
        private FeatureFlagSet requiredFeatures;

        public Builder(Supplier<T> constructor) {
            this.constructor = constructor;
            this.requiredFeatures = FeatureFlags.DEFAULT_FLAGS;
        }

        public Builder<T> requiredFeatures(FeatureFlagSet featureFlagSet) {
            this.requiredFeatures = featureFlagSet;
            return this;
        }

        public RoomUpgradeComponentType<T> build() {
            return new RoomUpgradeComponentType<>(constructor, requiredFeatures);
        }
    }
}
