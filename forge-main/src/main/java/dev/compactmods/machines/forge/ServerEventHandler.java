package dev.compactmods.machines.forge;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.inject.InjectField;
import dev.compactmods.machines.api.room.IRoomOwnerLookup;
import dev.compactmods.machines.api.room.registration.IRoomSpawnLookup;
import dev.compactmods.machines.api.upgrade.ILevelLoadedUpgradeListener;
import dev.compactmods.machines.forge.room.ForgeCompactRoomProvider;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.forge.room.upgrade.RoomUpgradeManager;
import dev.compactmods.machines.forge.util.AnnotationScanner;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerAboutToStart(final ServerAboutToStartEvent server) {
        final var modLog = LoggingUtil.modLog();

        modLog.debug("Starting addon scan and injection for server startup.");
        CompactMachines.getAddons().forEach(addon -> {
            final Supplier<IRoomOwnerLookup> ownerLookup = ForgeCompactRoomProvider::instance;
            final Supplier<IRoomSpawnLookup> spawnLookup = ForgeCompactRoomProvider::instance;

            final var injectableFields = AnnotationScanner.scanFields(addon, InjectField.class)
                    .filter(field -> field.canAccess(addon))
                    .collect(Collectors.toSet());

            if(injectableFields.isEmpty()) return;

            modLog.debug("Injecting lookup data into addon {} ...", addon.getClass());
            AnnotationScanner.injectFields(addon, ownerLookup, injectableFields);
            AnnotationScanner.injectFields(addon, spawnLookup, injectableFields);
        });
    }

    @SubscribeEvent
    public static void onWorldLoaded(final LevelEvent.Load evt) {
        if(evt.getLevel() instanceof ServerLevel compactDim && compactDim.dimension().equals(CompactDimension.LEVEL_KEY))
        {
            final var serv = compactDim.getServer();
            final var owBorder = serv.overworld().getWorldBorder();
            final var cwBorder = compactDim.getWorldBorder();

            // Filter border listeners down to the compact world, then remove them from the OW listener list
            final var listeners = owBorder.listeners.stream()
                    .filter(border -> border instanceof BorderChangeListener.DelegateBorderChangeListener)
                    .map(BorderChangeListener.DelegateBorderChangeListener.class::cast)
                    .filter(list -> list.worldBorder == cwBorder)
                    .collect(Collectors.toSet());

            for(var listener : listeners)
                owBorder.removeListener(listener);

            // Fix set compact world border if it was loaded weirdly
            cwBorder.setCenter(0, 0);
            cwBorder.setSize(WorldBorder.MAX_SIZE);
            PacketDistributor.DIMENSION.with(() -> CompactDimension.LEVEL_KEY)
                    .send(new ClientboundSetBorderSizePacket(cwBorder));


            // Room upgrade initialization
            final var levelUpgrades = RoomUpgradeManager.get(compactDim);
            final var roomInfo = CompactRoomProvider.instance(compactDim);

            levelUpgrades.implementing(ILevelLoadedUpgradeListener.class).forEach(inst -> {
                final var upg = inst.upgrade();
                roomInfo.forRoom(inst.room()).ifPresent(ri -> upg.onLevelLoaded(compactDim, ri));
            });

        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent evt) {
        final var player = evt.getEntity();
        if(player.level.dimension().equals(CompactDimension.LEVEL_KEY) && player instanceof ServerPlayer sp) {
            // Send a fake world border to the player instead of the "real" one in overworld
            sp.connection.send(new ClientboundInitializeBorderPacket(new WorldBorder()));
        }
    }

    @SubscribeEvent
    public static void onPlayerDimChange(final PlayerEvent.PlayerChangedDimensionEvent evt) {
        if(evt.getTo().equals(CompactDimension.LEVEL_KEY)) {
            final var player = evt.getEntity();
            if(player instanceof ServerPlayer sp) {
                // Send a fake world border to the player instead of the "real" one in overworld
                sp.connection.send(new ClientboundInitializeBorderPacket(new WorldBorder()));
            }
        }
    }
}
