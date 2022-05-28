package dev.compactmods.machines.room.menu;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.UIRegistration;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MachineRoomMenu extends AbstractContainerMenu {
    private final ChunkPos room;
    private final LevelBlockPosition machine;
    private StructureTemplate roomBlocks;

    public MachineRoomMenu(int win, ChunkPos room, LevelBlockPosition machine) {
        super(UIRegistration.MACHINE_MENU.get(), win);
        this.room = room;
        this.roomBlocks = new StructureTemplate();
        this.machine = machine;
    }

    public ChunkPos getRoom() {
        return room;
    }

    public LevelBlockPosition getMachine() {
        return machine;
    }

    public static MenuProvider makeProvider(MinecraftServer server, ChunkPos roomId, LevelBlockPosition machinePos) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(CompactMachines.MOD_ID + ".ui.room");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int winId, Inventory inv, Player player2) {
                var menu = new MachineRoomMenu(winId, roomId, machinePos);
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
}
