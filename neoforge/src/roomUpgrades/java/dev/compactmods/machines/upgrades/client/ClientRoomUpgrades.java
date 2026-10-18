package dev.compactmods.machines.upgrades.client;

import dev.compactmods.machines.upgrades.network.RoomUpgradesRequestPacket;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client-side copy of rooms' enabled upgrades, fetched on demand by {@link #request} (the server
 * keeps the authoritative set in {@code RoomSystems.ENABLED_UPGRADES}).
 */
public final class ClientRoomUpgrades {

    private ClientRoomUpgrades() {}

    private static final Map<String, List<Identifier>> UPGRADES = new HashMap<>();

    /** Drops what's known about {@code roomCode} and asks the server for a fresh copy. */
    public static void request(String roomCode) {
        UPGRADES.remove(roomCode);
        ClientPacketDistributor.sendToServer(new RoomUpgradesRequestPacket(roomCode));
    }

    public static void accept(String roomCode, List<Identifier> upgrades) {
        UPGRADES.put(roomCode, List.copyOf(upgrades));
    }

    /** {@return the room's enabled upgrades}, or empty until the server has answered a {@link #request}. */
    public static Optional<List<Identifier>> get(String roomCode) {
        return Optional.ofNullable(UPGRADES.get(roomCode));
    }
}
