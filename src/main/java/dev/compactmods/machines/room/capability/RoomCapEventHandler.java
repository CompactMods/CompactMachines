package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoomCapEventHandler {

    @SubscribeEvent
    static void onCapPlayerAttach(final AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof Player player))
            return;

        event.addCapability(
                new ResourceLocation(CompactMachines.MOD_ID, "room_history"),
                new PlayerRoomHistoryCapProvider(player));
    }
}
