package dev.compactmods.machines.rooms.capability;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoomCapEventHandler {

    @SubscribeEvent
    public static void onCapPlayerAttach(final AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getObject();
        event.addCapability(
                new ResourceLocation(CompactMachines.MOD_ID, "room_history"),
                new PlayerRoomHistoryCapProvider(player));
    }
}
