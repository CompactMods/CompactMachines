package dev.compactmods.machines.core;

import dev.compactmods.machines.core.util.KeyHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface WallConstants {

    ResourceKey<Block> BREAKABLE_WALL = KeyHelper.blockResKey("wall");
    ResourceKey<Block> SOLID_WALL = KeyHelper.blockResKey("solid_wall");

    /**
     * Applied to solid wall items.
     */
    TagKey<Item> TAG_SOLID_WALL_ITEMS = KeyHelper.itemTagKey("solid_walls");

    /**
     * Applied to solid walls and tunnel blocks.
     */
    TagKey<Block> TAG_SOLID_WALL_BLOCKS = KeyHelper.blockTag("solid_walls");


}
