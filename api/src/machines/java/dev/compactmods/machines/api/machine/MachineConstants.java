package dev.compactmods.machines.api.machine;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.util.KeyHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface MachineConstants {

    Identifier MACHINE_IDENTIFIER = CompactMachinesCore.identifier( "machine");

    TagKey<Block> MACHINE_BLOCK = KeyHelper.blockTag("machine");
    TagKey<Item> MACHINE_ITEM = KeyHelper.itemTagKey("machine");


}
