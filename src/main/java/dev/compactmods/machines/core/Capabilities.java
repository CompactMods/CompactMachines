package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.api.tunnels.connection.IRoomTunnels;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.rooms.capability.IRoomHistory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class Capabilities {

    public static final Capability<IMachineRoom> ROOM = CapabilityManager.get(new CapabilityToken<IMachineRoom>() {
    });

    public static final Capability<IRoomCapabilities> ROOM_CAPS = CapabilityManager.get(new CapabilityToken<IRoomCapabilities>() {
    });

    public static final Capability<IRoomTunnels> ROOM_TUNNELS = CapabilityManager.get(new CapabilityToken<IRoomTunnels>() {
    });

    public static final Capability<ITunnelConnection> TUNNEL_CONNECTION = CapabilityManager.get(new CapabilityToken<ITunnelConnection>() {
    });

    public static final Capability<IRoomHistory> ROOM_HISTORY = CapabilityManager.get(new CapabilityToken<IRoomHistory>() {
    });

    @SubscribeEvent
    void onCapRegistration(final RegisterCapabilitiesEvent evt) {
        evt.register(IMachineRoom.class);
        evt.register(IRoomCapabilities.class);
        evt.register(IRoomTunnels.class);
        evt.register(ITunnelConnection.class);
        evt.register(IRoomHistory.class);
    }
}
