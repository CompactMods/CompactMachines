package dev.compactmods.machines.upgrades.storage.tunnel;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.function.Function;
import java.util.function.Supplier;

public interface TunnelTranslations {

    Supplier<Component> SCREEN_TITLE = () -> Component.translatableWithFallback(IDs.SCREEN_TITLE, "Tunnel");
    Supplier<Component> NAME = () -> Component.translatableWithFallback(IDs.NAME, "Storage name");
    Supplier<Component> CREATE = () -> Component.translatableWithFallback(IDs.CREATE, "Create");
    Supplier<Component> SAVE = () -> Component.translatableWithFallback(IDs.SAVE, "Save");
    Function<Integer, Component> CAPACITY = (cap) -> Component.translatableWithFallback(IDs.CAPACITY, "Capacity: %s", cap);
    Function<Component, Component> CONTAINER_HINT = (item) -> Component.translatableWithFallback(IDs.CONTAINER_HINT, "Add %s to increase capacity", item);

    interface Preview {
        Supplier<Component> CONNECTED = () -> Component.translatableWithFallback(IDs.Preview.CONNECTED, "Connected machines:");
        Supplier<Component> NO_CONNECTIONS = () -> Component.translatableWithFallback(IDs.Preview.NO_CONNECTIONS, "No connected machines");
        /** Args: x, y, z, dimension, faces. */
        Function<Object[], Component> MACHINE = (args) -> Component.translatableWithFallback(IDs.Preview.MACHINE, "%s, %s, %s (%s): %s", args);
    }

    interface Errors {
        Supplier<Component> EMPTY_NAME = () -> Component.translatableWithFallback(IDs.Errors.EMPTY_NAME, "A storage needs a name.");
        Function<Integer, Component> NAME_TOO_LONG = (max) -> Component.translatableWithFallback(IDs.Errors.NAME_TOO_LONG, "Names can be at most %s characters.", max);
        Function<String, Component> DUPLICATE_NAME = (name) -> Component.translatableWithFallback(IDs.Errors.DUPLICATE_NAME, "This room already has a storage named '%s'.", name);
        Function<Component, Component> MISSING_CONTAINER = (item) -> Component.translatableWithFallback(IDs.Errors.MISSING_CONTAINER, "Put at least one %s in the slot.", item);
        Supplier<Component> MISSING_TUNNEL = () -> Component.translatableWithFallback(IDs.Errors.MISSING_TUNNEL, "You need a tunnel in hand.");
        Supplier<Component> NO_TYPE = () -> Component.translatableWithFallback(IDs.Errors.NO_TYPE, "Pick a tunnel type.");
        Supplier<Component> UNKNOWN_TYPE = () -> Component.translatableWithFallback(IDs.Errors.UNKNOWN_TYPE, "That tunnel type isn't available.");
        Function<String, Component> UNKNOWN_STORAGE = (name) -> Component.translatableWithFallback(IDs.Errors.UNKNOWN_STORAGE, "No storage named '%s'.", name);
        Supplier<Component> WALL_CHANGED = () -> Component.translatableWithFallback(IDs.Errors.WALL_CHANGED, "The wall changed; try again.");
    }

    interface IDs {
        String SCREEN_TITLE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("screen.title"));
        String NAME = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("screen.name"));
        String CREATE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("screen.create"));
        String SAVE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("screen.save"));
        String CAPACITY = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("screen.capacity"));
        String CONTAINER_HINT = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("screen.container_hint"));

        interface Preview {
            String CONNECTED = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("preview.connected"));
            String NO_CONNECTIONS = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("preview.no_connections"));
            String MACHINE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("preview.machine"));
        }

        interface Errors {
            String EMPTY_NAME = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.empty_name"));
            String NAME_TOO_LONG = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.name_too_long"));
            String DUPLICATE_NAME = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.duplicate_name"));
            String MISSING_CONTAINER = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.missing_container"));
            String MISSING_TUNNEL = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.missing_tunnel"));
            String NO_TYPE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.no_type"));
            String UNKNOWN_TYPE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.unknown_type"));
            String UNKNOWN_STORAGE = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.unknown_storage"));
            String WALL_CHANGED = Util.makeDescriptionId("tunnel", CompactMachinesCore.identifier("error.wall_changed"));
        }
    }
}
