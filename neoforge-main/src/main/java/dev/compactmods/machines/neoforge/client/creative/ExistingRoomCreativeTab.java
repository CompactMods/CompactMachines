package dev.compactmods.machines.neoforge.client.creative;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.machine.MachineCreator;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ExistingRoomCreativeTab {

    public static final HashMap<String, RoomInstance> KNOWN_ROOMS = new HashMap<>();

    @SubscribeEvent
    public static void onBuildTabContents(final BuildCreativeModeTabContentsEvent evt) {
        // TODO - Sync existing machine meta so stuff appears in creative
        if(evt.getTabKey() == CreativeTabs.EXISTING_MACHINES.getKey()) {
            for(var roomInfo : KNOWN_ROOMS.values()) {
                evt.accept(MachineCreator.boundToRoom(roomInfo.code(), roomInfo.defaultMachineColor()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
