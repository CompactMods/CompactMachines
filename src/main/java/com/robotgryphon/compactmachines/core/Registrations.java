package com.robotgryphon.compactmachines.core;

import com.robotgryphon.compactmachines.block.BlockCompactMachine;
import com.robotgryphon.compactmachines.block.BlockWall;
import com.robotgryphon.compactmachines.block.BlockWallBreakable;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.robotgryphon.compactmachines.item.ItemBlockWall;
import com.robotgryphon.compactmachines.item.ItemPersonalShrinkingDevice;

import static com.robotgryphon.compactmachines.CompactMachines.MODID;

public class Registrations {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    private static Block.Properties MACHINE_BLOCK_PROPS = Block.Properties
            .create(Material.IRON)
            .hardnessAndResistance(8.0F, 20.0F);

    public static final RegistryObject<Block> MACHINE_BLOCK_TINY = BLOCKS.register("machine_tiny", () ->
            new BlockCompactMachine(EnumMachineSize.TINY, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_SMALL = BLOCKS.register("machine_small", () ->
            new BlockCompactMachine(EnumMachineSize.SMALL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_NORMAL = BLOCKS.register("machine_normal", () ->
            new BlockCompactMachine(EnumMachineSize.NORMAL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_LARGE = BLOCKS.register("machine_large", () ->
            new BlockCompactMachine(EnumMachineSize.LARGE, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_GIANT = BLOCKS.register("machine_giant", () ->
            new BlockCompactMachine(EnumMachineSize.GIANT, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_MAXIMUM = BLOCKS.register("machine_maximum", () ->
            new BlockCompactMachine(EnumMachineSize.MAXIMUM, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_TINY = ITEMS.register("machine_tiny",
            () -> new BlockItem(MACHINE_BLOCK_TINY.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_SMALL = ITEMS.register("machine_small",
            () -> new BlockItem(MACHINE_BLOCK_SMALL.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_NORMAL = ITEMS.register("machine_normal",
            () -> new BlockItem(MACHINE_BLOCK_NORMAL.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_LARGE = ITEMS.register("machine_large",
            () -> new BlockItem(MACHINE_BLOCK_LARGE.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_GIANT = ITEMS.register("machine_giant",
            () -> new BlockItem(MACHINE_BLOCK_GIANT.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_MAXIMUM = ITEMS.register("machine_maximum",
            () -> new BlockItem(MACHINE_BLOCK_MAXIMUM.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Block> WALL_BLOCK = BLOCKS.register("wall", () ->
            new BlockWall(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(-1.0F, 3600000.8F)
                    .noDrops()));

    public static final RegistryObject<Block> BLOCK_BREAKABLE_WALL = BLOCKS.register("wall_breakable", () ->
            new BlockWallBreakable(Block.Properties
                    .create(Material.IRON)
                    .hardnessAndResistance(3.0f, 128.0f)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)));

    public static final RegistryObject<ItemPersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = ITEMS.register("personal_shrinking_device",
            () -> new ItemPersonalShrinkingDevice(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS)));

    public static final RegistryObject<Item> ITEM_WALL = ITEMS.register("wall", () ->
            new ItemBlockWall(WALL_BLOCK.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));

    public static final RegistryObject<Item> ITEM_BREAKABLE_WALL = ITEMS.register("wall_breakable", () ->
            new ItemBlockWall(BLOCK_BREAKABLE_WALL.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
