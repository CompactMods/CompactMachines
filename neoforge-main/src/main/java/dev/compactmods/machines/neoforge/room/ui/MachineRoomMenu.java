package dev.compactmods.machines.neoforge.room.ui;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.room.RoomBlocks;
import dev.compactmods.machines.neoforge.room.Rooms;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
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
        super(Rooms.MACHINE_MENU.get(), win);
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

    public static MenuProvider makeProvider(MinecraftServer server, String roomCode, GlobalPos machinePos) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable(Constants.MOD_ID + ".ui.room");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int winId, Inventory inv, Player player2) {
                try {
                    var menu = new MachineRoomMenu(winId, roomCode, machinePos, "Room Preview");
                    menu.roomBlocks = RoomBlocks.getInternalBlocks(server, roomCode).get(5, TimeUnit.SECONDS);
                    return menu;

                } catch (MissingDimensionException e) {
                    LoggingUtil.modLog().fatal("Error creating machine preview for {}.", machinePos, e);
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

    public static MachineRoomMenu createRoomMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
        data.readBlockPos();
        final var mach = data.readJsonWithCodec(GlobalPos.CODEC);
        final var room = data.readUtf();
        final boolean hasName = data.readBoolean();
        final var roomName = hasName ? data.readUtf() : "Room Preview";

        return new MachineRoomMenu(windowId, room, mach, roomName);
    }
}
