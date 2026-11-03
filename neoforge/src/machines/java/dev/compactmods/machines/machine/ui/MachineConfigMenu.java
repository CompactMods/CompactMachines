package dev.compactmods.machines.machine.ui;

import dev.compactmods.machines.machine.Machines;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasHost;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasHostMenu;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasHosts;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasSupplier;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * The machine configuration screen's menu: hosts the machine's configuration canvas (see
 * {@link dev.compactmods.machines.machine.config.MachineConfigCanvas}), plus the player's inventory.
 */
public class MachineConfigMenu extends AbstractContainerMenu implements CanvasHostMenu {

    private static final int REACH = 5;

    private final GlobalPos machinePos;
    private final CanvasHost canvas;

    public MachineConfigMenu(int containerId, Player player, GlobalPos machinePos, CanvasSupplier canvas) {
        this(containerId, player.getInventory(), machinePos, CanvasHosts.server(canvas, player));
    }

    public MachineConfigMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readGlobalPos(), CanvasHosts.read(extraData));
    }

    private MachineConfigMenu(int containerId, Inventory inventory, GlobalPos machinePos, CanvasHost canvas) {
        super(Machines.MACHINE_CONFIG_MENU.get(), containerId);
        this.machinePos = machinePos;
        this.canvas = canvas;
        this.addStandardInventorySlots(inventory, 10, 84);
    }

    /** Client-side data for the menu, read by the buffer constructor. Called by the menu's provider. */
    public static void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        final var configMenu = (MachineConfigMenu) menu;
        buffer.writeGlobalPos(configMenu.machinePos);
        configMenu.canvas.write(buffer);
    }

    public GlobalPos machinePos() {
        return machinePos;
    }

    @Override
    public Optional<CanvasHost> canvasHost() {
        return Optional.of(canvas);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        canvas.tick();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().dimension().equals(machinePos.dimension())
                && player.isWithinBlockInteractionRange(machinePos.pos(), REACH);
    }
}
