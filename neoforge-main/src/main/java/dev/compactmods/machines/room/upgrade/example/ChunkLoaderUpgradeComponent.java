package dev.compactmods.machines.room.upgrade.example;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponent;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponentType;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeRemovedEventListener;
import dev.compactmods.machines.room.upgrade.RoomUpgrades;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventHandler;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventListener;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class ChunkLoaderUpgradeComponent implements RoomUpgradeComponent, NeoForgeEventListener {

    public static final MapCodec<ChunkLoaderUpgradeComponent> CODEC = MapCodec.unit(ChunkLoaderUpgradeComponent::new);

    private final AppliedHandler appliedHandler;
    private final RemovedHandler removedHandler;

    private final NeoForgeEventHandler<ServerStartedEvent> neoServerStartedHandler;

    private static final Logger LOGGER = LogManager.getLogger();

    public ChunkLoaderUpgradeComponent() {
        this.appliedHandler = new AppliedHandler();
        this.removedHandler = new RemovedHandler();

        this.neoServerStartedHandler = new NeoForgeEventHandler<>() {
            @Override
            public Class<ServerStartedEvent> eventType() {
                return ServerStartedEvent.class;
            }

            @Override
            public void handle(RoomUpgradeInstance instance, ServerStartedEvent event) {
                LOGGER.info("Received server started");
                appliedHandler.handle(instance);
            }
        };
    }

    @Override
    public RoomUpgradeComponentType<?> getType() {
        return RoomUpgrades.CHUNK_LOADER.get();
    }

    @Override
    public Stream<RoomUpgradeComponentEvent> gatherEvents() {
        return Stream.of(this.appliedHandler, this.removedHandler);
    }

    @Override
    public Stream<NeoForgeEventHandler<? extends Event>> gatherNeoEvents() {
        return Stream.of(this.neoServerStartedHandler);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(Component.literal("Chunk Loader").withColor(CommonColors.LIGHT_GRAY));
    }

    public static class AppliedHandler implements UpgradeAppliedEventListener {
        @Override
        public void handle(RoomUpgradeInstance instance) {
            final var roomInstance = instance.roomInstance();

            roomInstance.boundaries().innerChunkPositions().forEach(chunkPos -> {
                CompactMachinesServer.CHUNK_TICKET_CONTROLLER
                        .forceChunk(instance.roomInstance().level(), instance.upgradeID(),
                                chunkPos.x, chunkPos.z, true, true);
            });
        }
    }

    public class RemovedHandler implements UpgradeRemovedEventListener {
        @Override
        public void handle(RoomUpgradeInstance instance) {
            final var roomInstance = instance.roomInstance();

            roomInstance.boundaries().innerChunkPositions().forEach(chunkPos -> {
                CompactMachinesServer.CHUNK_TICKET_CONTROLLER
                        .forceChunk(instance.roomInstance().level(), instance.upgradeID(),
                                chunkPos.x, chunkPos.z, false, true);
            });
        }
    }
}
