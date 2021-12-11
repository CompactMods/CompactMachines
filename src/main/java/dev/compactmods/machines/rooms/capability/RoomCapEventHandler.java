package dev.compactmods.machines.rooms.capability;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoomCapEventHandler {

    @SubscribeEvent
    public static void onCapPlayerAttach(final AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer) event.getObject();
        event.addCapability(
                new ResourceLocation(CompactMachines.MOD_ID, "room_history"),
                new PlayerRoomHistoryCapProvider(player));
    }
}
