package dev.compactmods.machines.api.room.registration;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface IRoomRegistrar {

    default MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    AABB getNextBoundaries(RoomTemplate template);

    IRoomBuilder builder();

    default RoomInstance createNew(RoomTemplate template, UUID owner) {
        return createNew(template, owner, override -> {});
    }

    default RoomInstance createNew(RoomTemplate template, UUID owner, Consumer<IRoomBuilder> override) {
        final Consumer<IRoomBuilder> preOverride = builder -> builder.defaultMachineColor(template.defaultMachineColor())
                .owner(owner)
                .boundaries(getNextBoundaries(template));

        // Make builder, set template defaults, then allow overrides
        final var b = builder();
        preOverride.andThen(override).accept(b);
        return b.build(server());
    }

    boolean isRegistered(String room);

    Optional<RoomInstance> get(String room);

    long count();

    Stream<String> allRoomCodes();

    Stream<RoomInstance> allRooms();
}
