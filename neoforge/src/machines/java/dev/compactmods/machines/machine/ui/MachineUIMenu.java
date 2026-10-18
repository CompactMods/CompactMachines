package dev.compactmods.machines.machine.ui;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.room.Rooms;
import dev.robotgryphon.screenlib.nodes.api.ScreenLibCapabilities;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasHost;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasHostMenu;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasHosts;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The machine screen's menu. Besides the core slot it hosts the bound room's upgrade canvas, when
 * the machine exposes one through {@link ScreenLibCapabilities#CANVAS} (see
 * {@link #writeClientSideData}).
 */
public class MachineUIMenu extends AbstractContainerMenu implements CanvasHostMenu {
    private final GlobalPos machinePos;
    private final @Nullable CompactMachineBlockEntity machine;

    private final @Nullable Slot coreSlot;
    private final @Nullable CanvasHost canvas;

    @ApiStatus.Internal
    public MachineUIMenu(int containerId, Player player, GlobalPos machinePos) {
        this(containerId, player, machinePos, serverCanvas(player, machinePos));
    }

    public MachineUIMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInv.player, extraData.readGlobalPos(),
                extraData.readBoolean() ? CanvasHosts.read(extraData) : null);
    }

    private MachineUIMenu(int containerId, Player player, GlobalPos machinePos, @Nullable CanvasHost canvas) {
        super(Machines.MACHINE_UI_MENU.get(), containerId);
        this.machinePos = machinePos;
        this.canvas = canvas;

        final var inv = player.getInventory();
        this.addStandardInventorySlots(inv, 10, 84);
//        this.addInventoryHotbarSlots(inv, 0, 112);

        final var level = player.level();
        if(level.getBlockEntity(machinePos.pos()) instanceof CompactMachineBlockEntity tile) {
            this.machine = tile;

            final var coreHandler = tile.coreHandler();
            this.coreSlot = this.addSlot(new ResourceHandlerSlot(coreHandler, coreHandler::set,0, 10, 10));
        } else {
            this.machine = null;
            this.coreSlot = null;
        }
    }

    private static @Nullable CanvasHost serverCanvas(Player player, GlobalPos machinePos) {
        final var canvas = player.level().getCapability(ScreenLibCapabilities.CANVAS, machinePos.pos());
        return canvas != null ? CanvasHosts.server(canvas, player) : null;
    }

    /** Client-side data for the menu, read by the buffer constructor. Called by the menu's provider. */
    public static void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        final var machineMenu = (MachineUIMenu) menu;
        buffer.writeGlobalPos(machineMenu.machinePos);
        buffer.writeBoolean(machineMenu.canvas != null);
        if (machineMenu.canvas != null)
            machineMenu.canvas.write(buffer);
    }

    @Override
    public Optional<CanvasHost> canvasHost() {
        return Optional.ofNullable(canvas);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (canvas != null)
            canvas.tick();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if(machinePos == null)
            return false;

        return player.isWithinBlockInteractionRange(machinePos.pos(), 5);
    }

    public GlobalPos machinePos() {
        return machinePos;
    }

    public Optional<String> currentRoom() {
        if(machine == null)
            return Optional.empty();

        return machine.connectedRoom();
    }

    public Optional<RoomTemplate> currentTemplate() {
        if(machine == null)
            return Optional.empty();

        final var currentCore = machine.coreHandler().getResource(0);
        if(currentCore.isEmpty())
            return Optional.empty();

        final var tid = currentCore.get(Rooms.DataComponents.ROOM_TEMPLATE_ID);
        if(tid == null)
            return Optional.empty();

        return RoomTemplateHelper.getTemplateOptional(machine.getLevel().registryAccess(), tid);
    }

    public @Nullable Slot getCoreSlot() {
        return coreSlot;
    }
}
