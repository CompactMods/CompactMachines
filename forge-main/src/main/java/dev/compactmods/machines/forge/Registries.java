package dev.compactmods.machines.forge;

import dev.compactmods.machines.api.core.CMRegistryKeys;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Registries {

    // Machines, Walls, Shrinking
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    // Tunnels
    public static final DeferredRegister<TunnelDefinition> TUNNEL_DEFINITIONS = DeferredRegister.create(TunnelDefinition.REGISTRY_KEY, MOD_ID);

    // UIRegistration
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    // MachineRoomUpgrades
    public static final DeferredRegister<RoomUpgrade> UPGRADES = DeferredRegister.create(CMRegistryKeys.ROOM_UPGRADES, MOD_ID);

    // Commands
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, MOD_ID);

    // LootFunctions
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, MOD_ID);

    public static DeferredRegister<RoomTemplate> ROOM_TEMPLATES = DeferredRegister
            .create(CMRegistryKeys.ROOM_TEMPLATES, Constants.MOD_ID);

    // Villagers
    public static final DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, Constants.MOD_ID);

    public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(ForgeRegistries.POI_TYPES, Constants.MOD_ID);

    public static void setup() {

    }
}
