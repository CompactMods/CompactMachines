package dev.compactmods.machines.room.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.CompactMachineItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyRoomBindingFunction extends LootItemConditionalFunction {

    protected CopyRoomBindingFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static Builder<?> binding() {
        return simpleBuilder(CopyRoomBindingFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        var data = ctx.getParam(LootContextParams.BLOCK_ENTITY);
        if(data instanceof CompactMachineBlockEntity machine) {
            machine.getConnectedRoom().ifPresent(room -> {
                CompactMachineItem.setRoom(stack, room);
            });
        }

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.COPY_ROOM_BINDING.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyRoomBindingFunction> {
        public void serialize(JsonObject json, CopyRoomBindingFunction func, JsonSerializationContext ctx) {
            super.serialize(json, func, ctx);
        }

        public CopyRoomBindingFunction deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new CopyRoomBindingFunction(conditions);
        }
    }
}
