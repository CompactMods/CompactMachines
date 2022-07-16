package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.item.PersonalShrinkingDevice;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.CompactMachineItem;
import dev.compactmods.machines.room.ItemBlockWall;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.wall.BreakableWallBlock;
import dev.compactmods.machines.wall.SolidWallBlock;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static dev.compactmods.machines.CompactMachines.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class Registration {

    // ================================================================================================================
    //   REGISTRIES
    // ================================================================================================================
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);


    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    static final BlockBehaviour.Properties MACHINE_BLOCK_PROPS = BlockBehaviour.Properties
            .of(Material.METAL)
            .strength(8.0F, 20.0F)
            .requiresCorrectToolForDrops();

    static final Supplier<Item.Properties> BASIC_ITEM_PROPS = () -> new Item.Properties()
            .tab(CompactMachines.COMPACT_MACHINES_ITEMS);

    // ================================================================================================================
    //   COMPACT MACHINE BLOCKS
    // ================================================================================================================
    public static final RegistryObject<Block> MACHINE_BLOCK_TINY = BLOCKS.register("machine_tiny", () ->
            new CompactMachineBlock(RoomSize.TINY, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_SMALL = BLOCKS.register("machine_small", () ->
            new CompactMachineBlock(RoomSize.SMALL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_NORMAL = BLOCKS.register("machine_normal", () ->
            new CompactMachineBlock(RoomSize.NORMAL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_LARGE = BLOCKS.register("machine_large", () ->
            new CompactMachineBlock(RoomSize.LARGE, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_GIANT = BLOCKS.register("machine_giant", () ->
            new CompactMachineBlock(RoomSize.GIANT, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_MAXIMUM = BLOCKS.register("machine_maximum", () ->
            new CompactMachineBlock(RoomSize.MAXIMUM, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_TINY = ITEMS.register("machine_tiny",
            () -> new CompactMachineItem(MACHINE_BLOCK_TINY.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_SMALL = ITEMS.register("machine_small",
            () -> new CompactMachineItem(MACHINE_BLOCK_SMALL.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_NORMAL = ITEMS.register("machine_normal",
            () -> new CompactMachineItem(MACHINE_BLOCK_NORMAL.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_LARGE = ITEMS.register("machine_large",
            () -> new CompactMachineItem(MACHINE_BLOCK_LARGE.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_GIANT = ITEMS.register("machine_giant",
            () -> new CompactMachineItem(MACHINE_BLOCK_GIANT.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_MAXIMUM = ITEMS.register("machine_maximum",
            () -> new CompactMachineItem(MACHINE_BLOCK_MAXIMUM.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<BlockEntityType<CompactMachineBlockEntity>> MACHINE_TILE_ENTITY = BLOCK_ENTITIES.register("compact_machine", () ->
            BlockEntityType.Builder.of(CompactMachineBlockEntity::new,
                            MACHINE_BLOCK_TINY.get(), MACHINE_BLOCK_SMALL.get(), MACHINE_BLOCK_NORMAL.get(),
                            MACHINE_BLOCK_LARGE.get(), MACHINE_BLOCK_GIANT.get(), MACHINE_BLOCK_MAXIMUM.get())
                    .build(null));


    // ================================================================================================================
    //   WALLS
    // ================================================================================================================

    public static final RegistryObject<Block> BLOCK_SOLID_WALL = BLOCKS.register("solid_wall", () ->
            new SolidWallBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)));

    public static final RegistryObject<Block> BLOCK_BREAKABLE_WALL = BLOCKS.register("wall", () ->
            new BreakableWallBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(3.0f, 128.0f)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = ITEMS.register("personal_shrinking_device",
            () -> new PersonalShrinkingDevice(BASIC_ITEM_PROPS.get()
                    .stacksTo(1)));

    public static final RegistryObject<Item> ITEM_SOLID_WALL = ITEMS.register("solid_wall", () ->
            new ItemBlockWall(BLOCK_SOLID_WALL.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> ITEM_BREAKABLE_WALL = ITEMS.register("wall", () ->
            new ItemBlockWall(BLOCK_BREAKABLE_WALL.get(), BASIC_ITEM_PROPS.get()));

    // ================================================================================================================
    //   DIMENSION
    // ================================================================================================================
    public static final ResourceKey<Level> COMPACT_DIMENSION = ResourceKey
            .create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "compact_world"));

    public static final ResourceKey<DimensionType> COMPACT_DIMENSION_DIM_TYPE = ResourceKey
            .create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(MOD_ID, "compact_world"));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
