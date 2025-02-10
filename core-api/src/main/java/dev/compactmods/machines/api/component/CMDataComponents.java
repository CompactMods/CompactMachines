package dev.compactmods.machines.api.component;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.room.upgrade.component.RoomUpgradeComponentList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public interface CMDataComponents {

    DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachines.MOD_ID);

    String KEY_ROOM_TEMPLATE = "room_template";
    String KEY_ROOM_CODE = "room_code";
    String KEY_MACHINE_COLOR = "machine_color";

    DeferredHolder<DataComponentType<?>, DataComponentType<RoomUpgradeComponentList>> UPGRADE_LIST_COMPONENT = DATA_COMPONENTS
            .registerComponentType("room_upgrades", (builder) -> builder
                    .persistent(RoomUpgradeComponentList.CODEC)
                    .networkSynchronized(RoomUpgradeComponentList.STREAM_CODEC));

    /**
     * Only on bound room items - given by a crafting process or when a bound machine block is broken
     */

    DeferredHolder<DataComponentType<?>, DataComponentType<String>> BOUND_ROOM_CODE = DATA_COMPONENTS
            .registerComponentType(KEY_ROOM_CODE, (builder) -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));


    DeferredHolder<DataComponentType<?>, DataComponentType<MachineColor>> MACHINE_COLOR = DATA_COMPONENTS
            .registerComponentType(KEY_MACHINE_COLOR, (builder) -> builder
                    .persistent(MachineColor.CODEC)
                    .networkSynchronized(MachineColor.STREAM_CODEC));

    /**
     * Only on new room items - IUnboundMachineItem
     */
    DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> ROOM_TEMPLATE_ID = DATA_COMPONENTS
            .registerComponentType(KEY_ROOM_TEMPLATE, (builder) -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC));

    DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> UPGRADE_INSTANCE_ID = DATA_COMPONENTS
            .registerComponentType("upgrade_id", (builder) -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    static void prepare() {}
}
