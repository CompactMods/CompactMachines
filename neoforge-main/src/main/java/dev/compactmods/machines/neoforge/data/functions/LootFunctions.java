package dev.compactmods.machines.neoforge.data.functions;

import dev.compactmods.machines.neoforge.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class LootFunctions {

    public static DeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_ROOM_BINDING = Registries.LOOT_FUNCS
            .register("copy_room_binding", () -> new LootItemFunctionType(CopyRoomBindingFunction.CODEC));

    public static void prepare() {

    }
}
