package dev.compactmods.machines.core;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.upgrade.ILevelLoadedUpgradeListener;
import dev.compactmods.machines.upgrade.RoomUpgradeManager;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onWorldLoaded(final LevelEvent.Load evt) {
        if(evt.getLevel() instanceof ServerLevel sl && sl.dimension().equals(CompactDimension.LEVEL_KEY))
        {
            final var serv = sl.getServer();
            final var owBorder = serv.overworld().getWorldBorder();
            final var cwBorder = sl.getWorldBorder();

            final var levelUpgrades = RoomUpgradeManager.get(sl);
            levelUpgrades.implementing(ILevelLoadedUpgradeListener.class).forEach(inst -> {
                final var upg = inst.upgrade();
                upg.onLevelLoaded(sl, inst.room());
            });

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

        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent evt) {
        final var player = evt.getEntity();
        if(player.level().dimension().equals(CompactDimension.LEVEL_KEY) && player instanceof ServerPlayer sp) {
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
