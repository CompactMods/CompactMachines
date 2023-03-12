package dev.compactmods.machines.forge.room.capability;

import dev.compactmods.machines.api.core.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoomCapEventHandler {

    @SubscribeEvent
    static void onCapPlayerAttach(final AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof Player player))
            return;

        event.addCapability(
                new ResourceLocation(Constants.MOD_ID, "room_history"),
                new PlayerRoomHistoryProvider());

        final var meta = new PlayerRoomMetadataProviderProvider();
        event.addCapability(new ResourceLocation(Constants.MOD_ID, "room_metadata"), meta);
    }
}
