package dev.compactmods.machines.forge.room.ui;

import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.forge.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MachineRoomMenu extends AbstractContainerMenu {
    private final String room;
    private String roomName;
    private final GlobalPos machine;
    private StructureTemplate roomBlocks;
    public boolean loadingBlocks;

    public MachineRoomMenu(int win, String room, GlobalPos machine, String roomName) {
        super(RoomUserInterfaceRegistration.MACHINE_MENU.get(), win);
        this.room = room;
        this.roomName = roomName;
        this.roomBlocks = new StructureTemplate();
        this.machine = machine;
        this.loadingBlocks = true;
    }

    public String getRoom() {
        return room;
    }

    public GlobalPos getMachine() {
        return machine;
    }

    public static MenuProvider makeProvider(MinecraftServer server, IRoomRegistration roomInfo, GlobalPos machinePos) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable(Constants.MOD_ID + ".ui.room");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int winId, Inventory inv, Player player2) {
                try {
                    final var title = Rooms.getRoomName(server, roomInfo.code());

                    var menu = new MachineRoomMenu(winId, roomInfo.code(), machinePos, title.orElse("Room Preview"));
                    menu.roomBlocks = Rooms.getInternalBlocks(server, roomInfo.code()).get(5, TimeUnit.SECONDS);
                    return menu;

                } catch (NonexistentRoomException | MissingDimensionException e) {
                    CompactMachines.LOGGER.fatal("Error creating machine preview for {}.", machinePos, e);
                    return null;
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slotInd) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Nullable
    public StructureTemplate getBlocks() {
        return roomBlocks;
    }

    public void setBlocks(StructureTemplate blocks) {
        this.roomBlocks = blocks;
        this.loadingBlocks = false;
    }

    public String getRoomName() {
        return roomName;
    }
}
