package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.block.BlockCompactMachine;
import dev.compactmods.machines.block.tiles.CompactMachineTile;
import dev.compactmods.machines.block.tiles.TunnelWallTile;
import dev.compactmods.machines.block.walls.BreakableWallBlock;
import dev.compactmods.machines.block.walls.SolidWallBlock;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.item.ItemBlockMachine;
import dev.compactmods.machines.item.ItemBlockWall;
import dev.compactmods.machines.item.ItemPersonalShrinkingDevice;
import dev.compactmods.machines.item.TunnelItem;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.tunnels.definitions.ItemTunnelDefinition;
import dev.compactmods.machines.tunnels.definitions.RedstoneInTunnelDefinition;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

import static dev.compactmods.machines.CompactMachines.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class Registration {
    // ================================================================================================================
    //   REGISTRIES
    // ================================================================================================================
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILES_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<TunnelDefinition> TUNNEL_DEFINITIONS = DeferredRegister.create(TunnelDefinition.class, MOD_ID);

    static {
        TUNNEL_DEFINITIONS.makeRegistry("tunnel_types",
                () -> new RegistryBuilder<TunnelDefinition>()
                        .setType(TunnelDefinition.class)
                        .tagFolder("tunnel_types"));
    }

    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    private static AbstractBlock.Properties MACHINE_BLOCK_PROPS = AbstractBlock.Properties
            .of(Material.METAL)
            .strength(8.0F, 20.0F)
            .harvestLevel(1)
            .harvestTool(ToolType.PICKAXE)
            .requiresCorrectToolForDrops();

    private static Supplier<Item.Properties> BASIC_ITEM_PROPS = () -> new Item.Properties()
            .tab(CompactMachines.COMPACT_MACHINES_ITEMS);

    // ================================================================================================================
    //   COMPACT MACHINE BLOCKS
    // ================================================================================================================
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
            () -> new ItemBlockMachine(MACHINE_BLOCK_TINY.get(), EnumMachineSize.TINY, BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_SMALL = ITEMS.register("machine_small",
            () -> new ItemBlockMachine(MACHINE_BLOCK_SMALL.get(), EnumMachineSize.SMALL, BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_NORMAL = ITEMS.register("machine_normal",
            () -> new ItemBlockMachine(MACHINE_BLOCK_NORMAL.get(), EnumMachineSize.NORMAL, BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_LARGE = ITEMS.register("machine_large",
            () -> new ItemBlockMachine(MACHINE_BLOCK_LARGE.get(), EnumMachineSize.LARGE, BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_GIANT = ITEMS.register("machine_giant",
            () -> new ItemBlockMachine(MACHINE_BLOCK_GIANT.get(), EnumMachineSize.GIANT, BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_MAXIMUM = ITEMS.register("machine_maximum",
            () -> new ItemBlockMachine(MACHINE_BLOCK_MAXIMUM.get(), EnumMachineSize.MAXIMUM, BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<TileEntityType<CompactMachineTile>> MACHINE_TILE_ENTITY = TILES_ENTITIES.register("compact_machine", () ->
            TileEntityType.Builder.of(CompactMachineTile::new,
                    MACHINE_BLOCK_TINY.get(), MACHINE_BLOCK_SMALL.get(), MACHINE_BLOCK_NORMAL.get(),
                    MACHINE_BLOCK_LARGE.get(), MACHINE_BLOCK_GIANT.get(), MACHINE_BLOCK_MAXIMUM.get())
                    .build(null));

    // ================================================================================================================
    //   WALLS
    // ================================================================================================================
    public static final RegistryObject<Block> BLOCK_TUNNEL_WALL = BLOCKS.register("tunnel_wall", () ->
            new TunnelWallBlock(AbstractBlock.Properties
                    .of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)
                    .noDrops()));

    public static final RegistryObject<Block> BLOCK_SOLID_WALL = BLOCKS.register("solid_wall", () ->
            new SolidWallBlock(AbstractBlock.Properties
                    .of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)
                    .noDrops()));

    public static final RegistryObject<Block> BLOCK_BREAKABLE_WALL = BLOCKS.register("wall", () ->
            new BreakableWallBlock(AbstractBlock.Properties
                    .of(Material.METAL)
                    .strength(3.0f, 128.0f)
                    .requiresCorrectToolForDrops()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)));

    public static final RegistryObject<ItemPersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = ITEMS.register("personal_shrinking_device",
            () -> new ItemPersonalShrinkingDevice(BASIC_ITEM_PROPS.get()
                    .stacksTo(1)));

    public static final RegistryObject<Item> ITEM_SOLID_WALL = ITEMS.register("solid_wall", () ->
            new ItemBlockWall(BLOCK_SOLID_WALL.get(), BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<Item> ITEM_BREAKABLE_WALL = ITEMS.register("wall", () ->
            new ItemBlockWall(BLOCK_BREAKABLE_WALL.get(), BASIC_ITEM_PROPS.get()));

    // ================================================================================================================
    //   TUNNELS
    // ================================================================================================================

    public static final RegistryObject<Item> ITEM_TUNNEL = ITEMS.register("tunnel", () ->
            new TunnelItem(BASIC_ITEM_PROPS.get()));

    public static final RegistryObject<TileEntityType<TunnelWallTile>> TUNNEL_WALL_TILE = TILES_ENTITIES.register("tunnel_wall", () ->
            TileEntityType.Builder.of(TunnelWallTile::new, BLOCK_TUNNEL_WALL.get())
                    .build(null));

    // ================================================================================================================
    //   TUNNEL TYPE DEFINITIONS
    // ================================================================================================================
    public static final RegistryObject<TunnelDefinition> ITEM_TUNNEL_DEF = TUNNEL_DEFINITIONS.register("item", ItemTunnelDefinition::new);

    public static final RegistryObject<TunnelDefinition> REDSTONE_IN_TUNNEL = TUNNEL_DEFINITIONS.register("redstone_in", RedstoneInTunnelDefinition::new);

    // public static final RegistryObject<TunnelDefinition> REDSTONE_OUT_TUNNEL = TUNNEL_DEFINITIONS.register("redstone_out", RedstoneOutTunnelDefinition::new);

    // ================================================================================================================
    //   DIMENSION
    // ================================================================================================================
    public static final RegistryKey<World> COMPACT_DIMENSION = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("compactmachines:compact_world"));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILES_ENTITIES.register(eventBus);

        TUNNEL_DEFINITIONS.register(eventBus);
    }
}
