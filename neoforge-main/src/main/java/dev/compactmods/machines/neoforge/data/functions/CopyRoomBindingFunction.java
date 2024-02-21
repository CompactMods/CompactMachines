package dev.compactmods.machines.neoforge.data.functions;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.machine.item.IBoundCompactMachineItem;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

public class CopyRoomBindingFunction implements LootItemFunction {

    public static final Codec<CopyRoomBindingFunction> CODEC = Codec.unit(new CopyRoomBindingFunction());

    @Override
    public ItemStack apply(ItemStack stack, LootContext ctx) {
        var state = ctx.getParam(LootContextParams.BLOCK_STATE);
        if(state.is(MachineConstants.MACHINE_BLOCK)) {
            var data = ctx.getParam(LootContextParams.BLOCK_ENTITY);
            if (data instanceof BoundCompactMachineBlockEntity machine && stack.getItem() instanceof IBoundCompactMachineItem bound) {
                bound.setColor(stack, machine.getColor());
                bound.setRoom(stack, machine.connectedRoom());
            }
        }

        return stack;
    }

    @Override
    public @NotNull LootItemFunctionType getType() {
        return LootFunctions.COPY_ROOM_BINDING.value();
    }
}
