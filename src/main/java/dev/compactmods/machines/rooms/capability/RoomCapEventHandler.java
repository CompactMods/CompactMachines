package dev.compactmods.machines.rooms.capability;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RoomCapEventHandler {

    @SubscribeEvent
    static void onCapPlayerAttach(final AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof ServerPlayer player))
            return;

        event.addCapability(
                new ResourceLocation(CompactMachines.MOD_ID, "room_history"),
                new PlayerRoomHistoryCapProvider(player));
    }

    @SubscribeEvent
    static void onCapChunkAttach(final AttachCapabilitiesEvent<LevelChunk> evt) {
        var chunk = evt.getObject();

        // do not attach room data to client levels
        if(chunk.getLevel().isClientSide)
            return;

        // only add room data to compact world chunks
        if(chunk.getLevel().dimension() != Registration.COMPACT_DIMENSION)
            return;

        boolean isRoom = chunk.getPos().x % 64 == 0 && chunk.getPos().z % 64 == 0;
        if(isRoom)
            evt.addCapability(new ResourceLocation(CompactMachines.MOD_ID, "room_info"), new RoomChunkDataProvider(chunk));
    }
}
