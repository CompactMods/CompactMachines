package com.robotgryphon.compactmachines.rooms.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
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
