package dev.compactmods.machines.preview.client;

import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.room.network.RoomPreviewSubscribePacket;
import dev.compactmods.machines.room.preview.client.ClientRoomEntities;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import dev.compactmods.machines.room.preview.client.RoomPreviewEntities;
import dev.compactmods.machines.room.preview.client.RoomPreviewMeshCache;
import dev.compactmods.machines.room.preview.client.gui.RoomPreviewGui;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Client-side driver that keeps the server informed of which rooms this client needs previews for.
 *
 * <p>Every frame, render-state extraction visits the visible machine block entities (the same hook
 * the pride-flag overlay uses); we collect the room codes their cores are bound to. Once a second we
 * compare that set to what we last told the server and, only if it changed, send a
 * {@link RoomPreviewSubscribePacket}. Subscribing to just the <em>visible</em> machines keeps the
 * server's scan set tight, and the whole-set-replaces-previous packet shape makes the debounce
 * trivial and idempotent.
 *
 * <p>This is the demand side of the preview pipeline; the actual mesh build and in-block rendering
 * (which consume {@link ClientRoomPreviews}) are registered separately by the machine renderer.
 */
public final class RoomPreviewClient {

    /** How often (in client ticks) we reconcile our subscription with the server. 20 ticks ≈ 1 s. */
    private static final int SUBSCRIBE_INTERVAL_TICKS = 20;

    /** Room codes bound to currently-visible machines, republished each frame by the render thread. */
    private static volatile Set<String> visibleRooms = Set.of();
    /** The last set we sent to the server (client-thread only). */
    private static Set<String> lastSent = Set.of();
    private static int tickCounter;

    private RoomPreviewClient() {}

    public static void registerEvents(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RoomPreviewClient::collectVisibleRooms);
        NeoForge.EVENT_BUS.addListener(RoomPreviewClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(RoomPreviewClient::onLoggingOut);

        // In-GUI previews (machine screen panel)
        modBus.addListener(RoomPreviewGui::registerRenderers);

        // Batched in-world preview rendering (manual pass, like the flag shaders).
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, RoomPreviewRenderer::extract);
        NeoForge.EVENT_BUS.addListener(RoomPreviewRenderer::prepareBuffers);
        NeoForge.EVENT_BUS.addListener(RoomPreviewRenderer::afterSolidBlocks);
        NeoForge.EVENT_BUS.addListener(RoomPreviewRenderer::afterOpaqueFeatures);
        NeoForge.EVENT_BUS.addListener(RoomPreviewRenderer::onSubmitCustomGeometry);
    }

    /** Render thread: gather the room codes of visible machines and publish them for the tick loop. */
    private static void collectVisibleRooms(final ExtractLevelRenderStateEvent event) {
        final Set<String> found = new HashSet<>();
        event.getLevelExtractor().iterateVisibleBlockEntities(be -> {
            if (be instanceof CompactMachineBlockEntity machine)
                machine.connectedRoom().ifPresent(found::add);
        });
        visibleRooms = found;
    }

    /** Client thread: tick preview entities every tick; reconcile the subscription set periodically. */
    private static void onClientTick(final ClientTickEvent.Post event) {
        // Every tick: advance preview entities' animation via their own tick().
        RoomPreviewEntities.tickAll();
        // ... and the preview block entities' client tickers (items moving through pipes, ...)
        RoomPreviewMeshCache.tickAll();

        if (++tickCounter < SUBSCRIBE_INTERVAL_TICKS) return;
        tickCounter = 0;

        final Set<String> current = new HashSet<>(visibleRooms);
        current.addAll(ClientRoomPreviews.watched());
        if (current.equals(lastSent)) return;

        // Entities stop arriving for rooms we drop, so forget them now rather than freeze them in place
        for (String dropped : lastSent)
            if (!current.contains(dropped))
                ClientRoomEntities.remove(dropped);

        lastSent = current;
        dev.compactmods.machines.core.CompactMachinesCore.modLog().info("[RoomPreview] client subscribing to {}", current);
        ClientPacketDistributor.sendToServer(new RoomPreviewSubscribePacket(new ArrayList<>(current)));
    }

    private static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        visibleRooms = Set.of();
        lastSent = Set.of();
        tickCounter = 0;
        ClientRoomPreviews.clear();
        RoomPreviewMeshCache.clear();
        ClientRoomEntities.clear();
        RoomPreviewEntities.clear();
    }
}
