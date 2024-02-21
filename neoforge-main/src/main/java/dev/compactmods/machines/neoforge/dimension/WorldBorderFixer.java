package dev.compactmods.machines.neoforge.dimension;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class WorldBorderFixer {

    @SubscribeEvent
    public static void onWorldLoaded(final LevelEvent.Load evt) {
        if (evt.getLevel() instanceof ServerLevel compactDim && compactDim.dimension().equals(CompactDimension.LEVEL_KEY))
            WorldBorderFixer.sendWorldBorderListenerOverrides(compactDim.getServer(), compactDim);
    }

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent evt) {
        if(evt.getEntity() instanceof ServerPlayer sp && CompactDimension.isLevelCompact(sp.level()))
            WorldBorderFixer.sendClientWorldBorderFix(sp);
    }

    @SubscribeEvent
    public static void onPlayerDimChange(final PlayerEvent.PlayerChangedDimensionEvent evt) {
        if (evt.getTo().equals(CompactDimension.LEVEL_KEY) && evt.getEntity() instanceof ServerPlayer sp)
            WorldBorderFixer.sendClientWorldBorderFix(sp);
    }

    public static void sendWorldBorderListenerOverrides(MinecraftServer serv, ServerLevel compactDim) {
        final var owBorder = serv.overworld().getWorldBorder();
        final var cwBorder = compactDim.getWorldBorder();

        // Filter border listeners down to the compact world, then remove them from the OW listener list
        final var listeners = owBorder.listeners.stream()
                .filter(border -> border instanceof BorderChangeListener.DelegateBorderChangeListener)
                .map(BorderChangeListener.DelegateBorderChangeListener.class::cast)
                .filter(list -> list.worldBorder == cwBorder)
                .collect(Collectors.toSet());

        for (var listener : listeners)
            owBorder.removeListener(listener);

        // Fix set compact world border if it was loaded weirdly
        cwBorder.setCenter(0, 0);
        cwBorder.setSize(WorldBorder.MAX_SIZE);
        PacketDistributor.DIMENSION.with(CompactDimension.LEVEL_KEY)
                .send(new ClientboundSetBorderSizePacket(cwBorder));
    }

    public static void sendClientWorldBorderFix(ServerPlayer player) {
        // Send a fake world border to the player instead of the "real" one in overworld
        player.connection.send(new ClientboundInitializeBorderPacket(new WorldBorder()));
    }
}
