package dev.compactmods.machines.room.data;

import dev.compactmods.machines.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.RegistryObject;

public class LootFunctions {

    public static RegistryObject<LootItemFunctionType> COPY_ROOM_BINDING = Registries.LOOT_FUNCS.register("copy_room_binding",
            () -> new LootItemFunctionType(new CopyRoomBindingFunction.Serializer()));

    public static void prepare() {

    }
}
