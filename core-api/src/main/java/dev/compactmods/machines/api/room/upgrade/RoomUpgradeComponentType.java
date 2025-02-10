package dev.compactmods.machines.api.room.upgrade;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record RoomUpgradeComponentType<T extends RoomUpgradeComponent>(
        Supplier<T> constructor,
        MapCodec<T> codec,
        FeatureFlagSet requiredFeatures,
        Predicate<ItemStack> itemstackFilter
) implements FeatureElement {

    public static final ResourceKey<Registry<RoomUpgradeComponentType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(CompactMachines.modRL("room_upgrades"));

    public static <T extends RoomUpgradeComponent> Builder<T> builder(Supplier<T> constructor, MapCodec<T> codec) {
        return new Builder<>(constructor, codec);
    }

    public boolean canApplyTo(ItemStack item) {
        return itemstackFilter == null || itemstackFilter.test(item);
    }

    public static class Builder<T extends RoomUpgradeComponent> {
        private final Supplier<T> constructor;
        private final MapCodec<T> codec;
        private FeatureFlagSet requiredFeatures;
        private Predicate<ItemStack> itemPredicate;

        public Builder(Supplier<T> constructor, MapCodec<T> codec) {
            this.constructor = constructor;
            this.codec = codec;
            this.requiredFeatures = FeatureFlags.DEFAULT_FLAGS;
        }

        public Builder<T> requiredFeatures(FeatureFlagSet featureFlagSet) {
            this.requiredFeatures = featureFlagSet;
            return this;
        }

        public Builder<T> itemPredicate(Predicate<ItemStack> predicate) {
            this.itemPredicate = predicate;
            return this;
        }

        public RoomUpgradeComponentType<T> build() {
            return new RoomUpgradeComponentType<>(constructor, codec, requiredFeatures, itemPredicate);
        }

    }
}