package dev.compactmods.machines.machine.config;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasSupplier;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * The machine configuration canvas of a Compact Machine — where its room's named storages are bound
 * to the machine's sides. Provided for machine block entities by the main mod (which can see the
 * storage system); {@code null} for an unbound machine.
 */
public final class MachineConfigCanvas {

    private MachineConfigCanvas() {}

    public static final BlockCapability<CanvasSupplier, Void> CAPABILITY =
            BlockCapability.createVoid(CompactMachinesCore.identifier("machine_config_canvas"), CanvasSupplier.class);
}
