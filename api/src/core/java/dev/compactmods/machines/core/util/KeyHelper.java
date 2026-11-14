package dev.compactmods.machines.core.util;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class KeyHelper {

  public static ResourceKey<Block> blockResKey(String id) {
	 return ResourceKey.create(Registries.BLOCK, CompactMachinesCore.identifier(id));
  }

  @NotNull
  public static TagKey<Item> itemTagKey(String id) {
	 return TagKey.create(Registries.ITEM, CompactMachinesCore.identifier(id));
  }

  public static TagKey<Block> blockTag(String id) {
	 return TagKey.create(Registries.BLOCK, CompactMachinesCore.identifier(id));
  }

  public static ResourceKey<Item> itemResKey(String id) {
	 return ResourceKey.create(Registries.ITEM, CompactMachinesCore.identifier(id));
  }
}
