package org.dave.compactmachines3.core;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.dave.compactmachines3.block.BlockCompactMachine;
import org.dave.compactmachines3.block.BlockWall;
import org.dave.compactmachines3.block.BlockWallBreakable;
import org.dave.compactmachines3.item.ItemPersonalShrinkingDevice;

import static org.dave.compactmachines3.CompactMachines3.MODID;

public class Registrations {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<BlockCompactMachine> MACHINE_BLOCK = BLOCKS.register("machine", BlockCompactMachine::new);
    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM = ITEMS.register("machine",
            () -> new BlockItem(MACHINE_BLOCK.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<BlockWall> WALL_BLOCK = BLOCKS.register("wall", BlockWall::new);
    public static final RegistryObject<BlockWallBreakable> BREAKABLE_WALL_BLOCK = BLOCKS.register("breakable_wall", BlockWallBreakable::new);

    public static final RegistryObject<ItemPersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = ITEMS.register("personal_shrinking_device",
            () -> new ItemPersonalShrinkingDevice(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS)));

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
