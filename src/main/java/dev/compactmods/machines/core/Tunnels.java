package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.tunnel.TunnelItem;
import dev.compactmods.machines.tunnel.TunnelWallBlock;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import dev.compactmods.machines.tunnel.definitions.FluidTunnel;
import dev.compactmods.machines.tunnel.definitions.ForgeEnergyTunnel;
import dev.compactmods.machines.tunnel.definitions.ItemTunnel;
import dev.compactmods.machines.tunnel.definitions.UnknownTunnel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static dev.compactmods.machines.CompactMachines.MOD_ID;

public class Tunnels {

    // region Setup

    public static final ResourceLocation DEFINITIONS_RL = new ResourceLocation(MOD_ID, "tunnel_types");
    public static final DeferredRegister<TunnelDefinition> DEFINITIONS = DeferredRegister.create(DEFINITIONS_RL, MOD_ID);

    public static final Supplier<IForgeRegistry<TunnelDefinition>> TUNNEL_DEF_REGISTRY = DEFINITIONS.makeRegistry(() -> new RegistryBuilder<TunnelDefinition>()
            .setName(DEFINITIONS_RL));

    public static void init(IEventBus bus) {
        DEFINITIONS.register(bus);
    }
    // endregion

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
    public static final RegistryObject<TunnelDefinition> UNKNOWN = DEFINITIONS.register("unknown", UnknownTunnel::new);

    public static final RegistryObject<Item> ITEM_TUNNEL = Registration.ITEMS.register("tunnel", () ->
            new TunnelItem(Registration.BASIC_ITEM_PROPS.get()));

    // ================================================================================================================
    //   TUNNEL TYPE DEFINITIONS
    // ================================================================================================================
    public static final RegistryObject<TunnelDefinition> ITEM_TUNNEL_DEF = DEFINITIONS.register("item", ItemTunnel::new);

    public static final RegistryObject<TunnelDefinition> FLUID_TUNNEL_DEF = DEFINITIONS.register("fluid", FluidTunnel::new);

    public static final RegistryObject<TunnelDefinition> FORGE_ENERGY = DEFINITIONS.register("energy", ForgeEnergyTunnel::new);

    // ================================================================================================================
    //   TUNNEL BLOCKS / TILES
    // ================================================================================================================
    public static final RegistryObject<Block> BLOCK_TUNNEL_WALL = Registration.BLOCKS.register("tunnel_wall", () ->
            new TunnelWallBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)));

    public static final RegistryObject<BlockEntityType<TunnelWallEntity>> TUNNEL_BLOCK_ENTITY = Registration.BLOCK_ENTITIES
            .register("tunnel_wall", () -> BlockEntityType.Builder.of(TunnelWallEntity::new, BLOCK_TUNNEL_WALL.get()).build(null));

    public static ResourceLocation getRegistryId(TunnelDefinition definition) {
        final var reg = TUNNEL_DEF_REGISTRY.get();
        if (!reg.containsValue(definition)) return new ResourceLocation(MOD_ID, "unknown");
        return reg.getKey(definition);
    }
}
