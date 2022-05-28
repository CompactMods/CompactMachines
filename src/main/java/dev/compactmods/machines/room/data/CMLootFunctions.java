package dev.compactmods.machines.room.data;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.room.data.CopyRoomBindingFunction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CMLootFunctions {

    public static LootItemFunctionType COPY_ROOM_BINDING;

    @SubscribeEvent
    static void onLootSerializing(final RegistryEvent.Register<Block> evt) {
        COPY_ROOM_BINDING = Registry.register(Registry.LOOT_FUNCTION_TYPE,
                new ResourceLocation(CompactMachines.MOD_ID, "copy_room_binding"),
                new LootItemFunctionType(new CopyRoomBindingFunction.Serializer()));
    }
}
