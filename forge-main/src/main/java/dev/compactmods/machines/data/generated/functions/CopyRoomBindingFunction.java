package dev.compactmods.machines.data.generated.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
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
        var state = ctx.getParam(LootContextParams.BLOCK_STATE);
        if(state.is(CMTags.MACHINE_BLOCK)) {
            var data = ctx.getParam(LootContextParams.BLOCK_ENTITY);
            if (data instanceof CompactMachineBlockEntity machine) {
                machine.basicRoomInfo().ifPresent(room -> {
                    ICompactMachineItem.setColor(stack, room.color());
                    BoundCompactMachineItem.setRoom(stack, room.code());
                });
            }
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
