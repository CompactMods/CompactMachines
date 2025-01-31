package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.shrinking.component.ShrinkingDeviceConfiguration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class Shrinking {

    public static final DeferredItem<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = CMRegistries.ITEMS.register("personal_shrinking_device",
            () -> new PersonalShrinkingDevice(new Item.Properties()
                    .component(DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG)
                    .stacksTo(1)));

    public static final DeferredItem<Item> SHRINKING_MODULE = CMRegistries.ITEMS.register("shrinking_module", CMRegistries::basicItem);
    public static final DeferredItem<Item> ENLARGING_MODULE = CMRegistries.ITEMS.register("enlarging_module", CMRegistries::basicItem);
    // public static final DeferredItem<Item> RESIZING_MODULE = Registries.ITEMS.register("resizing_module", Registries::basicItem);

    public interface DataComponents {
        String KEY_SHRINKING_CONFIG = "shrinking_device";

        DeferredHolder<DataComponentType<?>, DataComponentType<ShrinkingDeviceConfiguration>> SHRINKING_CONFIG = CMDataComponents.DATA_COMPONENTS
                .registerComponentType(KEY_SHRINKING_CONFIG, (builder) -> builder
                        .persistent(ShrinkingDeviceConfiguration.CODEC)
                        .networkSynchronized(ShrinkingDeviceConfiguration.STREAM_CODEC));

        static void prepare() {
        }
    }

    public static void prepare() {
        DataComponents.prepare();
    }
}

