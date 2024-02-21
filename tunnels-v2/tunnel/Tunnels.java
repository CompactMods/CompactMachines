package dev.compactmods.machines.neoforge.tunnel;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.tunnel.definitions.UnknownTunnel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.neoforged.registries.IForgeRegistry;
import net.neoforged.registries.RegistryBuilder;
import net.neoforged.registries.RegistryObject;

import java.util.function.Supplier;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Tunnels {

    public static final Supplier<IForgeRegistry<TunnelDefinition>> TUNNEL_DEF_REGISTRY = Registries.TUNNEL_DEFINITIONS
            .makeRegistry(RegistryBuilder::new);

    public static boolean isRegistered(ResourceLocation id) {
        return TUNNEL_DEF_REGISTRY.get().containsKey(id);
    }

    public static TunnelDefinition getDefinition(ResourceKey<TunnelDefinition> id) {
        if (isRegistered(id.location())) return TUNNEL_DEF_REGISTRY.get().getValue(id.location());
        CompactMachines.LOGGER.warn("Unknown tunnel requested: {}", id);
        return Tunnels.UNKNOWN.get();
    }

    public static TunnelDefinition getDefinition(ResourceLocation id) {
        if (isRegistered(id)) return TUNNEL_DEF_REGISTRY.get().getValue(id);
        CompactMachines.LOGGER.warn("Unknown tunnel requested: {}", id);
        return Tunnels.UNKNOWN.get();
    }

    // ================================================================================================================
    //   TUNNELS
    // ================================================================================================================
    public static final ResourceKey<TunnelDefinition> UNKNOWN_KEY = ResourceKey.create(TunnelDefinition.REGISTRY_KEY,
            new ResourceLocation(Constants.MOD_ID, "unknown"));

    public static final RegistryObject<TunnelDefinition> UNKNOWN = Registries.TUNNEL_DEFINITIONS
            .register("unknown", UnknownTunnel::new);

    public static final RegistryObject<Item> ITEM_TUNNEL = Registries.ITEMS.register("tunnel", () ->
            new TunnelItem(new Item.Properties().tab(CompactMachines.COMPACT_MACHINES_ITEMS)));

    // ================================================================================================================
    //   TUNNEL BLOCKS / TILES
    // ================================================================================================================
    public static final RegistryObject<Block> BLOCK_TUNNEL_WALL = Registries.BLOCKS.register("tunnel_wall", () ->
            new TunnelWallBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)));

    public static final RegistryObject<BlockEntityType<TunnelWallEntity>> TUNNEL_BLOCK_ENTITY = Registries.BLOCK_ENTITIES
            .register("tunnel_wall", () -> BlockEntityType.Builder.of(TunnelWallEntity::new, BLOCK_TUNNEL_WALL.get())
                    .build(null));

    public static ResourceKey<TunnelDefinition> getRegistryKey(TunnelDefinition definition) {
        final var reg = TUNNEL_DEF_REGISTRY.get();
        return reg.getResourceKey(definition).orElse(UNKNOWN_KEY);
    }

    public static ResourceLocation getRegistryId(TunnelDefinition definition) {
        final var reg = TUNNEL_DEF_REGISTRY.get();
        if (!reg.containsValue(definition)) return new ResourceLocation(Constants.MOD_ID, "unknown");
        return reg.getKey(definition);
    }

    public static void prepare() {
    }
}
