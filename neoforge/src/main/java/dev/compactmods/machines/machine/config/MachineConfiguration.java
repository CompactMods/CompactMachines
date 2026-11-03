package dev.compactmods.machines.machine.config;

import dev.compactmods.machines.CMDataAttachments;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.upgrades.storage.nodes.StorageNodes;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasDefinition;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasSupplier;
import dev.robotgryphon.screenlib.nodes.api.state.CanvasState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * The machine configuration canvas and what it drives: {@link MachineSideNode}s on a bound machine's
 * canvas bind its room's named storages (or any other handlers) to the machine's sides, exposed as the
 * item and fluid block capabilities for pipes and machines next to it.
 *
 * <p>The canvas layout and the resulting {@link MachineSideBindings} are saved on the machine block
 * entity. The bindings are what the capabilities read, so they work without the canvas ever being
 * evaluated again — it's only rebuilt when someone opens the configuration screen. Lives in the main mod
 * because it ties machines to the storage system.
 */
public final class MachineConfiguration {

    private MachineConfiguration() {}

    /** The configuration canvas layout (nodes and connections). */
    public static final Supplier<AttachmentType<CanvasState>> CANVAS_STATE =
            CMDataAttachments.ATTACHMENT_TYPES.register("machine_config_canvas", () -> AttachmentType
                    .builder(CanvasState::empty)
                    .serialize(CanvasState.MAP_CODEC)
                    .build());

    /** What the machine exposes on each side. */
    public static final Supplier<AttachmentType<MachineSideBindings>> SIDE_BINDINGS =
            CMDataAttachments.ATTACHMENT_TYPES.register("machine_sides", () -> AttachmentType
                    .builder(() -> MachineSideBindings.EMPTY)
                    .serialize(MachineSideBindings.MAP_CODEC)
                    .build());

    /** A machine's canvas, and the room it was built for; rebuilt if the machine is rebound. */
    private record Built(String room, CanvasSupplier canvas) {}

    private static final Map<CompactMachineBlockEntity, Built> CANVASES = new WeakHashMap<>();

    public static void init(IEventBus modBus) {
        modBus.addListener(MachineConfiguration::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        final var machines = Machines.BlockEntities.MACHINE.get();

        event.registerBlockEntity(MachineConfigCanvas.CAPABILITY, machines, (machine, _) -> canvas(machine));

        event.registerBlockEntity(Capabilities.Item.BLOCK, machines, (machine, side) -> {
            if (side == null || !(machine.getLevel() instanceof ServerLevel level))
                return null;
            final var access = machine.getData(SIDE_BINDINGS).items().get(side);
            return access == null ? null : access.resolve(level.getServer()).orElse(null);
        });

        event.registerBlockEntity(Capabilities.Fluid.BLOCK, machines, (machine, side) -> {
            if (side == null || !(machine.getLevel() instanceof ServerLevel level))
                return null;
            final var access = machine.getData(SIDE_BINDINGS).fluids().get(side);
            return access == null ? null : access.resolve(level.getServer()).orElse(null);
        });
    }

    /** {@return the machine's configuration canvas}, or null if it isn't bound to a room (or not on the server) */
    private static @Nullable CanvasSupplier canvas(CompactMachineBlockEntity machine) {
        if (!(machine.getLevel() instanceof ServerLevel level))
            return null;

        final var rooms = RoomCapabilities.REGISTRY.getCapability(level.getServer());
        final var room = machine.connectedRoom().flatMap(code -> rooms == null ? Optional.empty() : rooms.get(code));
        if (room.isEmpty()) {
            CANVASES.remove(machine);
            return null;
        }

        final var existing = CANVASES.get(machine);
        if (existing != null && existing.room().equals(room.get().code()))
            return existing.canvas();

        final var definition = new CanvasDefinition(machine.getData(CANVAS_STATE));
        definition.allow(MachineSideNode.KEY);
        StorageNodes.registerChoices(definition, room.get());

        final var supplier = CanvasSupplier.of(definition)
                .watchAll(MachineSideNode.KEY, nodes -> bind(machine, MachineSideBindings.from(nodes)))
                .onChanged(() -> {
                    machine.setData(CANVAS_STATE, definition.state());
                    machine.setChanged();
                });

        supplier.refresh(level.getServer());
        CANVASES.put(machine, new Built(room.get().code(), supplier));
        return supplier;
    }

    /** Stores new side bindings and tells neighbours to look up the machine's capabilities again. */
    private static void bind(CompactMachineBlockEntity machine, MachineSideBindings bindings) {
        if (bindings.equals(machine.getData(SIDE_BINDINGS)))
            return;

        machine.setData(SIDE_BINDINGS, bindings);
        machine.setChanged();
        if (machine.getLevel() != null)
            machine.getLevel().invalidateCapabilities(machine.getBlockPos());
    }
}
