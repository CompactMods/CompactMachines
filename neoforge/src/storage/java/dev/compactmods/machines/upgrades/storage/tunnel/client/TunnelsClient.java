package dev.compactmods.machines.upgrades.storage.tunnel.client;

import dev.compactmods.machines.room.preview.client.RoomPreviewOverlay;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.upgrades.storage.tunnel.block.TunnelWallBlockEntity;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigMenu;
import dev.robotgryphon.screenlib.ui.api.UiScreenBuilder;
import dev.robotgryphon.screenlib.ui.api.UiScreens;
import dev.robotgryphon.screenlib.ui.api.layout.HorizontalAnchor;
import dev.robotgryphon.screenlib.ui.api.layout.Placement;
import dev.robotgryphon.screenlib.ui.api.layout.VerticalAnchor;
import dev.robotgryphon.screenlib.ui.api.panel.InventoryPanel;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;

/** Client side of tunnels: the config screen, the tunnel wall's tint and the room preview markers. */
public final class TunnelsClient {

    private static final int MARGIN = 5;
    private static final int WHITE = 0xFFFFFFFF;

    private TunnelsClient() {}

    public static void registerEvents(IEventBus modBus) {
        modBus.addListener(TunnelsClient::registerMenuScreens);
        modBus.addListener(TunnelsClient::registerBlockTints);
        RoomPreviewOverlay.register(TunnelPreviewOverlay.INSTANCE);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        UiScreens.register(event, Tunnels.CONFIG_MENU.get(), TunnelsClient::screen);
    }

    /** The config panel in the middle of the screen, with the player's inventory below it for the container slot. */
    private static UiScreenBuilder screen(TunnelConfigMenu menu, Inventory inventory, Component title) {
        return UiScreens.builder(title)
                .panel(Placement.anchored(HorizontalAnchor.CENTER, VerticalAnchor.CENTER, 0, -40),
                        host -> new TunnelConfigPanel(menu))
                .panel(Placement.anchored(HorizontalAnchor.CENTER, VerticalAnchor.BOTTOM, 0, -MARGIN), host -> {
                    final var panel = new InventoryPanel(menu, inventory.getDisplayName());
                    panel.closable(false);
                    panel.open();
                    return panel;
                });
    }

    /** Tints the tunnel overlay (tintindex 0) with the bound tunnel type's color. */
    private static final BlockTintSource TUNNEL_TINT = new BlockTintSource() {
        @Override
        public int color(@NonNull BlockState state) {
            return WHITE;
        }

        @Override
        public int colorInWorld(@NonNull BlockState state, BlockAndTintGetter level, @NonNull BlockPos pos) {
            return level.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnel ? ARGB.opaque(tunnel.color()) : WHITE;
        }

        @Override
        public int colorAsTerrainParticle(@NonNull BlockState state, BlockAndTintGetter level, @NonNull BlockPos pos) {
            return colorInWorld(state, level, pos);
        }
    };

    private static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(TUNNEL_TINT), Tunnels.Blocks.TUNNEL_WALL.get());
    }
}
