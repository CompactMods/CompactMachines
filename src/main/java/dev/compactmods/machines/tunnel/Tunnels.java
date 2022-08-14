package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.tunnel.definitions.FluidTunnel;
import dev.compactmods.machines.tunnel.definitions.ForgeEnergyTunnel;
import dev.compactmods.machines.tunnel.definitions.ItemTunnel;
import dev.compactmods.machines.tunnel.definitions.UnknownTunnel;
import dev.compactmods.machines.tunnel.definitions.redstone.RedstoneInTunnelDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Tunnels {

    public static final Supplier<IForgeRegistry<TunnelDefinition>> TUNNEL_DEF_REGISTRY = Registries.TUNNEL_DEFINITIONS.makeRegistry(RegistryBuilder::new);

    public static boolean isRegistered(ResourceLocation id) {
        return TUNNEL_DEF_REGISTRY.get().containsKey(id);
    }

    public static TunnelDefinition getDefinition(ResourceLocation id) {
        if (isRegistered(id)) return TUNNEL_DEF_REGISTRY.get().getValue(id);
        CompactMachines.LOGGER.warn("Unknown tunnel requested: {}", id);
        return Tunnels.UNKNOWN.get();
    }

    // ================================================================================================================
    //   TUNNELS
    // ================================================================================================================
    public static final RegistryObject<TunnelDefinition> UNKNOWN = Registries.TUNNEL_DEFINITIONS.register("unknown", UnknownTunnel::new);

    public static final RegistryObject<Item> ITEM_TUNNEL = Registries.ITEMS.register("tunnel", () ->
            new TunnelItem(new Item.Properties().tab(CompactMachines.COMPACT_MACHINES_ITEMS)));

    // ================================================================================================================
    //   TUNNEL TYPE DEFINITIONS
    // ================================================================================================================
    public static final RegistryObject<TunnelDefinition> ITEM_TUNNEL_DEF = Registries.TUNNEL_DEFINITIONS.register("item", ItemTunnel::new);

    public static final RegistryObject<TunnelDefinition> FLUID_TUNNEL_DEF = Registries.TUNNEL_DEFINITIONS.register("fluid", FluidTunnel::new);

    public static final RegistryObject<TunnelDefinition> FORGE_ENERGY = Registries.TUNNEL_DEFINITIONS.register("energy", ForgeEnergyTunnel::new);

    public static final RegistryObject<TunnelDefinition> REDSTONE_IN = Registries.TUNNEL_DEFINITIONS
            .register("redstone_in", RedstoneInTunnelDefinition::new);

    // ================================================================================================================
    //   TUNNEL BLOCKS / TILES
    // ================================================================================================================
    public static final RegistryObject<Block> BLOCK_TUNNEL_WALL = Registries.BLOCKS.register("tunnel_wall", () ->
            new TunnelWallBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)));

    public static final RegistryObject<BlockEntityType<TunnelWallEntity>> TUNNEL_BLOCK_ENTITY = Registries.BLOCK_ENTITIES
            .register("tunnel_wall", () -> BlockEntityType.Builder.of(TunnelWallEntity::new, BLOCK_TUNNEL_WALL.get()).build(null));

    public static ResourceLocation getRegistryId(TunnelDefinition definition) {
        final var reg = TUNNEL_DEF_REGISTRY.get();
        if (!reg.containsValue(definition)) return new ResourceLocation(MOD_ID, "unknown");
        return reg.getKey(definition);
    }

    public static void prepare() {
    }
}
