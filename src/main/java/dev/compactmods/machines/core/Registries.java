package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static dev.compactmods.machines.CompactMachines.MOD_ID;

public class Registries {

    // Machines, Walls, Shrinking
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    // Tunnels
    public static final ResourceLocation TYPES_REG_KEY = new ResourceLocation(MOD_ID, "tunnel_types");
    public static final DeferredRegister<TunnelDefinition> TUNNEL_DEFINITIONS = DeferredRegister.create(TYPES_REG_KEY, MOD_ID);

    // UIRegistration
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    // MachineRoomUpgrades
    public static final ResourceKey<Registry<RoomUpgrade>> ROOM_UPGRADES_REG_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation(CompactMachines.MOD_ID, "room_upgrades"));
    public static final DeferredRegister<RoomUpgrade> UPGRADES = DeferredRegister.create(ROOM_UPGRADES_REG_KEY, MOD_ID);

    // Graph
    public static final ResourceKey<Registry<IGraphNodeType>> NODES_REG_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation(CompactMachines.MOD_ID, "graph_nodes"));
    public static final ResourceKey<Registry<IGraphEdgeType>> EDGES_REG_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation(CompactMachines.MOD_ID, "graph_edges"));
    public static final DeferredRegister<IGraphNodeType> NODE_TYPES = DeferredRegister.create(NODES_REG_KEY, MOD_ID);
    public static final DeferredRegister<IGraphEdgeType> EDGE_TYPES = DeferredRegister.create(EDGES_REG_KEY, MOD_ID);

    // Commands
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, MOD_ID);

    // LootFunctions
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, MOD_ID);

    public static void setup() {

    }
}
