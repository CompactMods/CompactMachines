package dev.compactmods.machines.room.data;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CompactMachinesLootFunctions {

    private static final DeferredRegister<LootItemFunctionType> LOOT_FUNCS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, CompactMachines.MOD_ID);

    public static RegistryObject<LootItemFunctionType> COPY_ROOM_BINDING = LOOT_FUNCS.register("copy_room_binding",
            () -> new LootItemFunctionType(new CopyRoomBindingFunction.Serializer()));

    public static void init(IEventBus bus) {
        LOOT_FUNCS.register(bus);
    }
}
