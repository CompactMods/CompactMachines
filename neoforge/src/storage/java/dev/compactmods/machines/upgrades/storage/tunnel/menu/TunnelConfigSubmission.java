package dev.compactmods.machines.upgrades.storage.tunnel.menu;

import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.compactmods.machines.upgrades.storage.tunnel.NamedStorageService;
import dev.compactmods.machines.upgrades.storage.tunnel.RoomStorageDefinition;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelTranslations;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.upgrades.storage.tunnel.block.TunnelWallBlockEntity;
import dev.compactmods.machines.upgrades.storage.tunnel.network.TunnelConfigStatePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

/**
 * Applies a tunnel config screen submission on the server. Create mode makes (or re-binds) a named
 * storage and turns the wall into a tunnel, taking one tunnel item and the slot's containers; edit
 * mode renames the bound storage and adds any containers. Either way the player gets a
 * {@link TunnelConfigStatePacket} with the outcome.
 */
public final class TunnelConfigSubmission {

    private TunnelConfigSubmission() {}

    public static void submit(ServerPlayer player, TunnelConfigMenu menu, String name, Optional<ResourceKey<TunnelType>> type) {
        final var level = player.level();
        final var pos = menu.wallPos();

        final var storages = Tunnels.roomAt(level, pos)
                .map(room -> room.getCapability(StorageCapabilities.NAMED_STORAGES));

        if (storages.isEmpty()) {
            fail(player, menu, TunnelTranslations.Errors.WALL_CHANGED.get());
            return;
        }

        if (menu.state().isEmpty())
            create(player, menu, storages.get(), name, type);
        else
            edit(player, menu, storages.get(), menu.state().get().storage(), name);
    }

    private static void create(ServerPlayer player, TunnelConfigMenu menu, NamedStorageService storages,
                               String name, Optional<ResourceKey<TunnelType>> typeKey) {
        final var level = player.level();
        final var pos = menu.wallPos();

        if (!level.getBlockState(pos).is(Rooms.Blocks.SOLID_WALL.get())) {
            fail(player, menu, TunnelTranslations.Errors.WALL_CHANGED.get());
            return;
        }

        if (typeKey.isEmpty()) {
            fail(player, menu, TunnelTranslations.Errors.NO_TYPE.get());
            return;
        }

        final var typeError = storages.checkType(typeKey.get());
        if (typeError.isPresent()) {
            fail(player, menu, typeError.get());
            return;
        }

        final var tunnelHand = tunnelHand(player);
        if (tunnelHand.isEmpty() && !player.isCreative()) {
            fail(player, menu, TunnelTranslations.Errors.MISSING_TUNNEL.get());
            return;
        }

        final var type = storages.tunnelType(typeKey.get()).orElseThrow().value();
        final int containers = menu.containerCount(type.container().value());

        switch (storages.create(name, typeKey.get(), containers, pos)) {
            case NamedStorageService.Result.Failure(var error) -> fail(player, menu, error);
            case NamedStorageService.Result.Success(var storage) -> {
                menu.takeContainers(containers);
                if (!player.isCreative())
                    tunnelHand.ifPresent(hand -> player.getItemInHand(hand).shrink(1));

                level.setBlock(pos, Tunnels.Blocks.TUNNEL_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
                if (level.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnel)
                    tunnel.bind(storage.name(), typeKey.get(), menu.face(), type.color());

                level.invalidateCapabilities(pos);
                succeed(player, menu, storages, storage);
            }
        }
    }

    private static void edit(ServerPlayer player, TunnelConfigMenu menu, NamedStorageService storages,
                             RoomStorageDefinition current, String newName) {
        final var level = player.level();
        final var pos = menu.wallPos();

        if (!(level.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnel) || !tunnel.storageName().equals(current.name())) {
            fail(player, menu, TunnelTranslations.Errors.WALL_CHANGED.get());
            return;
        }

        final var type = storages.tunnelType(current.type());
        if (type.isEmpty()) {
            fail(player, menu, TunnelTranslations.Errors.UNKNOWN_TYPE.get());
            return;
        }

        final int containers = menu.containerCount(type.get().value().container().value());
        switch (storages.edit(current.name(), newName, containers)) {
            case NamedStorageService.Result.Failure(var error) -> fail(player, menu, error);
            case NamedStorageService.Result.Success(var storage) -> {
                menu.takeContainers(containers);
                if (!storage.name().equals(tunnel.storageName()))
                    tunnel.rename(storage.name());

                // Its capacity may have changed; let neighbours look again
                level.invalidateCapabilities(pos);
                succeed(player, menu, storages, storage);
            }
        }
    }

    /** {@return the hand holding a tunnel}, preferring the main hand */
    private static Optional<InteractionHand> tunnelHand(ServerPlayer player) {
        for (var hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).is(Tunnels.Items.TUNNEL.get()))
                return Optional.of(hand);
        }
        return Optional.empty();
    }

    private static void succeed(ServerPlayer player, TunnelConfigMenu menu, NamedStorageService storages, RoomStorageDefinition storage) {
        final var state = Optional.of(new TunnelConfigState(storage, storages.capacity(storage)));
        menu.update(state, Optional.empty());
        PacketDistributor.sendToPlayer(player, new TunnelConfigStatePacket(menu.containerId, state, Optional.empty()));
    }

    private static void fail(ServerPlayer player, TunnelConfigMenu menu, Component error) {
        menu.update(Optional.empty(), Optional.of(error));
        PacketDistributor.sendToPlayer(player, new TunnelConfigStatePacket(menu.containerId, Optional.empty(), Optional.of(error)));
    }
}
