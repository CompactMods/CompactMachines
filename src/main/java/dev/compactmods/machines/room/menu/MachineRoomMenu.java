package dev.compactmods.machines.room.menu;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.UIRegistration;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MachineRoomMenu extends AbstractContainerMenu {
    private final int machineId;
    private final ChunkPos room;
    private StructureTemplate roomBlocks;

    public MachineRoomMenu(int win, int machineId, ChunkPos room) {
        super(UIRegistration.MACHINE_MENU.get(), win);
        this.machineId = machineId;
        this.room = room;
        this.roomBlocks = new StructureTemplate();
    }

    public ChunkPos getRoom() {
        return room;
    }

    public static MenuProvider makeProvider(MinecraftServer server, int machine, ChunkPos roomId) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(CompactMachines.MOD_ID + ".ui.room");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int winId, Inventory inv, Player player2) {
                var menu = new MachineRoomMenu(winId, machine, roomId);
                try {
                    menu.roomBlocks = Rooms.getInternalBlocks(server, roomId);
                } catch (MissingDimensionException | NonexistentRoomException e) {
                    return null;
                }

                return menu;
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
    }

    public int getMachineId() {
        return machineId;
    }
}
