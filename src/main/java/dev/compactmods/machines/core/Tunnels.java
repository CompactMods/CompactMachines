package dev.compactmods.machines.core;

import static dev.compactmods.machines.CompactMachines.MOD_ID;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.item.TunnelItem;
import dev.compactmods.machines.tunnel.ItemImportTunnel;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class Tunnels {

    // region Setup
    public static IForgeRegistry<TunnelDefinition> TUNNEL_DEF_REGISTRY;
    public static final DeferredRegister<TunnelDefinition> TUNNEL_DEFINITIONS = DeferredRegister.create(TunnelDefinition.class, MOD_ID);

    static {
        TUNNEL_DEFINITIONS.makeRegistry("tunnel_types",
                () -> new RegistryBuilder<TunnelDefinition>()
                        .setType(TunnelDefinition.class)
                        .tagFolder("tunnel_types"));
    }

    @SubscribeEvent
    public void onTunnelDefs(RegistryEvent.Register<TunnelDefinition> reg) {
        TUNNEL_DEF_REGISTRY = reg.getRegistry();
    }

    public static void init(IEventBus bus) {
        TUNNEL_DEFINITIONS.register(bus);
    }
    // endregion

    // ================================================================================================================
    //   TUNNELS
    // ================================================================================================================

    public static final RegistryObject<Item> ITEM_TUNNEL = Registration.ITEMS.register("tunnel", () ->
            new TunnelItem(Registration.BASIC_ITEM_PROPS.get()));

//    public static final RegistryObject<BlockEntityType<TunnelWallTile>> TUNNEL_WALL_TILE = BLOCK_ENTITIES.register("tunnel_wall", () ->
//            BlockEntityType.Builder.of(TunnelWallTile::new, BLOCK_TUNNEL_WALL.get())
//                    .build(null));

    // ================================================================================================================
    //   TUNNEL TYPE DEFINITIONS
    // ================================================================================================================
    public static final RegistryObject<TunnelDefinition> ITEM_IN_DEF = TUNNEL_DEFINITIONS.register("item_in", ItemImportTunnel::new);

//    public static final RegistryObject<TunnelDefinition> REDSTONE_IN_TUNNEL = TUNNEL_DEFINITIONS.register("redstone_in", RedstoneInTunnelDefinition::new);

    // public static final RegistryObject<TunnelDefinition> REDSTONE_OUT_TUNNEL = TUNNEL_DEFINITIONS.register("redstone_out", RedstoneOutTunnelDefinition::new);
}
