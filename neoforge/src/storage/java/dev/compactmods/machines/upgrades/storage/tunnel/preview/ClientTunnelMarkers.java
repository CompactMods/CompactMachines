package dev.compactmods.machines.upgrades.storage.tunnel.preview;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side store of the latest {@link RoomTunnelsPacket} per room, read by the preview overlay. */
public final class ClientTunnelMarkers {

    private static final Map<String, List<TunnelMarker>> MARKERS = new ConcurrentHashMap<>();

    private ClientTunnelMarkers() {}

    static void accept(String room, List<TunnelMarker> tunnels) {
        MARKERS.put(room, tunnels);
    }

    /** {@return the tunnels last received for {@code room}} */
    public static List<TunnelMarker> get(String room) {
        return MARKERS.getOrDefault(room, List.of());
    }

    public static void clear() {
        MARKERS.clear();
    }
}
