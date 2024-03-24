package dev.compactmods.machines.neoforge;

import dev.compactmods.machines.api.room.IRoomRegistrar;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.owner.IRoomOwners;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.data.RoomAttachmentDataManager;
import dev.compactmods.machines.room.RoomApiInstance;
import dev.compactmods.machines.room.RoomRegistrar;
import dev.compactmods.machines.room.spatial.GraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerAboutToStart(final ServerAboutToStartEvent server) {
        final var modLog = LoggingUtil.modLog();

        // modLog.debug("Starting addon scan and injection for server startup.");
//        CompactMachines.getAddons().forEach(addon -> {
//            final Supplier<IRoomOwnerLookup> ownerLookup = ForgeCompactRoomProvider::instance;
//            final Supplier<IRoomSpawnLookup> spawnLookup = ForgeCompactRoomProvider::instance;
//
//            final var injectableFields = AnnotationScanner.scanFields(addon, InjectField.class)
//                    .filter(field -> field.canAccess(addon))
//                    .collect(Collectors.toSet());
//
//            if(injectableFields.isEmpty()) return;
//
//            modLog.debug("Injecting lookup data into addon {} ...", addon.getClass());
//            AnnotationScanner.injectFields(addon, ownerLookup, injectableFields);
//            AnnotationScanner.injectFields(addon, spawnLookup, injectableFields);
//        });
    }

    @SubscribeEvent
    public static void onServerStarting(final ServerStartingEvent evt) {
        final var modLog = LoggingUtil.modLog();

        try {
            modLog.debug("Setting up room API and data...");
            MinecraftServer server = evt.getServer();

            // Set up room API
            RoomApi.INSTANCE = RoomApiInstance.forServer(server);

            // Set up room data attachments for Neo
            RoomAttachmentDataManager.instance(server);

            modLog.debug("Completed setting up room API and data.");
        } catch (MissingDimensionException e) {
            modLog.fatal("Failed to set up room API instance; dimension error.", e);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(final ServerStoppingEvent evt) {
        RoomAttachmentDataManager.instance().ifPresent(RoomAttachmentDataManager::save);
    }

    @SubscribeEvent
    public static void onWorldLoaded(final LevelEvent.Load evt) {
        if (evt.getLevel() instanceof ServerLevel compactDim && compactDim.dimension().equals(CompactDimension.LEVEL_KEY)) {
            // FIXME Room upgrade initialization
//            final var levelUpgrades = RoomUpgradeManager.get(compactDim);
//            final var roomInfo = CompactRoomProvider.instance(compactDim);
//
//            levelUpgrades.implementing(ILevelLoadedUpgradeListener.class).forEach(inst -> {
//                final var upg = inst.upgrade();
//                roomInfo.forRoom(inst.room()).ifPresent(ri -> upg.onLevelLoaded(compactDim, ri));
//            });
        }
    }
}