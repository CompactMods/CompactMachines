package dev.compactmods.machines.upgrades.storage.tunnel;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.tunnel.block.TunnelWallBlock;
import dev.compactmods.machines.upgrades.storage.tunnel.block.TunnelWallBlockEntity;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigMenu;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

/**
 * Tunnels: items put on a room's inner walls that create and expose the room's named storages.
 *
 * <p>Using a tunnel on a solid wall opens a {@link TunnelConfigMenu} to name a new storage and pick its
 * {@link TunnelType}; submitting turns the wall into a {@link TunnelWallBlock} bound to that storage.
 * Sneak-using the tunnel wall with an empty hand unbinds it (the storage keeps its data) and pops the
 * tunnel back off.
 */
public interface Tunnels {

    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachinesCore.MOD_ID);
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactMachinesCore.MOD_ID);
    DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, CompactMachinesCore.MOD_ID);

    interface Blocks {
        ResourceKey<Block> TUNNEL_WALL_KEY = ResourceKey.create(Registries.BLOCK, CompactMachinesCore.identifier("tunnel_wall"));

        DeferredBlock<TunnelWallBlock> TUNNEL_WALL = BLOCKS.register("tunnel_wall", () ->
                new TunnelWallBlock(BlockBehaviour.Properties.of()
                        .setId(TUNNEL_WALL_KEY)
                        .strength(-1.0F, 3600000.8F)
                        .sound(SoundType.METAL)
                        .lightLevel((state) -> 15)
                        .noLootTable()));

        static void prepare() {
        }
    }

    interface Items {
        DeferredItem<TunnelItem> TUNNEL = ITEMS.register("tunnel", () ->
                new TunnelItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachinesCore.identifier("tunnel")))));

        static void prepare() {
        }
    }

    interface BlockEntities {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<TunnelWallBlockEntity>> TUNNEL_WALL = BLOCK_ENTITIES
                .register("tunnel_wall", () -> new BlockEntityType<>(TunnelWallBlockEntity::new, Blocks.TUNNEL_WALL.get()));

        static void prepare() {
        }
    }

    DeferredHolder<MenuType<?>, MenuType<TunnelConfigMenu>> CONFIG_MENU = MENUS.register("tunnel_config",
            () -> IMenuTypeExtension.create(TunnelConfigMenu::new));

    static void init(IEventBus modBus) {
        Blocks.prepare();
        Items.prepare();
        BlockEntities.prepare();
        RoomNamedStorages.init();

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);

        modBus.addListener(Tunnels::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, BlockEntities.TUNNEL_WALL.get(),
                (wall, side) -> wall.handler(Capabilities.Item.BLOCK, side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, BlockEntities.TUNNEL_WALL.get(),
                (wall, side) -> wall.handler(Capabilities.Fluid.BLOCK, side));
    }

    /** {@return the room whose walls include {@code pos}}, if {@code level} is the compact dimension on the server */
    static Optional<RoomInstance> roomAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || !CompactDimension.isLevelCompact(level))
            return Optional.empty();

        final var chunks = RoomCapabilities.CHUNK_MANAGER.getCapability(serverLevel.getServer());
        if (chunks == null)
            return Optional.empty();

        return chunks.findRoomByChunk(ChunkPos.containing(pos))
                .filter(room -> room.boundaries().outerBounds().contains(Vec3.atCenterOf(pos)));
    }

    /** {@return whether {@code face} of the wall at {@code pos} points into the room's interior} */
    static boolean facesInward(RoomInstance room, BlockPos pos, Direction face) {
        return room.boundaries().innerBounds().contains(Vec3.atCenterOf(pos.relative(face)));
    }

    /** Opens the tunnel config screen for the wall at {@code pos}: create mode when {@code state} is empty, edit mode otherwise. */
    static void openConfig(ServerPlayer player, BlockPos pos, Direction face, Optional<TunnelConfigState> state) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return TunnelTranslations.SCREEN_TITLE.get();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                return new TunnelConfigMenu(containerId, inventory, pos, face, state);
            }

            @Override
            public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
                TunnelConfigMenu.writeClientSideData(menu, buffer);
            }
        });
    }
}
