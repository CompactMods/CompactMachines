package dev.compactmods.machines.core;

import java.util.function.Supplier;
import static dev.compactmods.machines.CompactMachines.MOD_ID;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.tunnel.TunnelItem;
import dev.compactmods.machines.tunnel.TunnelWallBlock;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import dev.compactmods.machines.tunnel.UnknownTunnel;
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

public class Tunnels {

    // region Setup

    @SuppressWarnings("unchecked")
    public static final DeferredRegister<TunnelDefinition> DEFINITIONS = DeferredRegister.create((Class) TunnelDefinition.class, MOD_ID);

    @SuppressWarnings("unchecked")
    public static final Supplier<IForgeRegistry<TunnelDefinition>> TUNNEL_DEF_REGISTRY = DEFINITIONS.makeRegistry("tunnel_types",
            () -> new RegistryBuilder<TunnelDefinition>()
                    .setType((Class) TunnelDefinition.class)
                    .tagFolder("tunnel_types"));

    public static void init(IEventBus bus) {
        DEFINITIONS.register(bus);
    }
    // endregion

    public static boolean isRegistered(ResourceLocation id) {
        return TUNNEL_DEF_REGISTRY.get().containsKey(id);
    }

    public static TunnelDefinition getDefinition(ResourceLocation id) {
        return isRegistered(id) ? TUNNEL_DEF_REGISTRY.get().getValue(id) : Tunnels.UNKNOWN.get();
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
//    public static final RegistryObject<TunnelDefinition> ITEM_IN_DEF = DEFINITIONS.register("item_in", ItemTunnel::new);

    // ================================================================================================================
    //   TUNNEL BLOCKS / TILES
    // ================================================================================================================
    public static final RegistryObject<Block> BLOCK_TUNNEL_WALL = Registration.BLOCKS.register("tunnel_wall", () ->
            new TunnelWallBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.CLAY)
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)
                    .noDrops()));

    public static final RegistryObject<BlockEntityType<TunnelWallEntity>> TUNNEL_BLOCK_ENTITY = Registration.BLOCK_ENTITIES
            .register("tunnel_wall", () -> BlockEntityType.Builder.of(TunnelWallEntity::new, BLOCK_TUNNEL_WALL.get()).build(null));
}
