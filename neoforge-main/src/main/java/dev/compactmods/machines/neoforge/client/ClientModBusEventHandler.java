package dev.compactmods.machines.neoforge.client;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.machine.client.MachineColors;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.machine.block.UnboundCompactMachineEntity;
import dev.compactmods.machines.neoforge.room.ui.preview.MachineRoomScreen;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.room.ui.upgrades.RoomUpgradeScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusEventHandler {

    @SubscribeEvent
    public static void onItemColors(final RegisterColorHandlersEvent.Item colors) {
        colors.register(MachineColors.ITEM, Machines.BOUND_MACHINE_BLOCK_ITEM.get());
        colors.register(MachineColors.ITEM, Machines.UNBOUND_MACHINE_BLOCK_ITEM.get());
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.register(MachineColors.BLOCK, Machines.MACHINE_BLOCK.get());
        colors.register(ClientModBusEventHandler::unboundMachineColor, Machines.UNBOUND_MACHINE_BLOCK.get());
    }

    private static int unboundMachineColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        return switch (tintIndex) {
            case 0 -> level.getBlockEntity(pos) instanceof UnboundCompactMachineEntity unbound ? unbound.getColor() : 0xFFFFFFFF;
            default -> 0xFFFFFFFF;
        };
    }

    @SubscribeEvent
    public static void onKeybindRegistration(final RegisterKeyMappingsEvent evt) {
        evt.register(RoomExitKeyMapping.MAPPING);
    }

    @SubscribeEvent
    public static void onClientSetup(final RegisterMenuScreensEvent evt) {
        evt.register(Rooms.MACHINE_MENU.get(), MachineRoomScreen::new);
        evt.register(Rooms.ROOM_UPGRADE_MENU.get(), RoomUpgradeScreen::new);
    }
}
