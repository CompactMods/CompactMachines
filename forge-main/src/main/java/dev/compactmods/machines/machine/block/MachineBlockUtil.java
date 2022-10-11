package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.machine.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.item.LegacyCompactMachineItem;
import dev.compactmods.machines.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.room.RoomCapabilities;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public class MachineBlockUtil {

    @Nonnull
    static InteractionResult tryRoomTeleport(Level level, BlockPos pos, Player player, MinecraftServer server, ServerPlayer sp) {
        // Try teleport to compact machine dimension
        if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
            tile.roomInfo().ifPresentOrElse(room -> {
                try {
                    PlayerUtil.teleportPlayerIntoMachine(level, player, tile.getLevelPosition(), room);
                } catch (MissingDimensionException e) {
                    e.printStackTrace();
                }
            }, () -> {
                final var state = level.getBlockState(pos);
                RoomTemplate template = RoomTemplate.INVALID_TEMPLATE;
                if(state.is(LegacySizedCompactMachineBlock.LEGACY_MACHINES_TAG)) {
                    if(state.getBlock() instanceof LegacySizedCompactMachineBlock b)
                        template = LegacySizedCompactMachineBlock.getLegacyTemplate(b.getSize());
                } else {
                    template = tile.getRoomTemplate().orElse(RoomTemplate.INVALID_TEMPLATE);
                }

                createAndEnterRoom(player, server, template, tile);
                // AdvancementTriggers.getTriggerForMachineClaim(size).trigger(sp);
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static void createAndEnterRoom(Player owner, MinecraftServer server, RoomTemplate template, CompactMachineBlockEntity machine) {
        try {
            final var compactDim = CompactDimension.forServer(server);
            if(template.equals(RoomTemplate.INVALID_TEMPLATE)) {
                CompactMachines.LOGGER.fatal("Tried to create and enter an invalidly-registered room. Something went very wrong!");
                return;
            }

            final var roomInfo = CompactRoomProvider.instance(compactDim);
            final var newRoom = roomInfo.registerNew(builder -> builder
                    .setColor(template.color())
                    .setDimensions(template.dimensions())
                    .setOwner(owner.getUUID()));

            // Generate a new machine room
            CompactStructureGenerator.generateRoom(compactDim, template.dimensions(), newRoom.center());

            // If template specified, prefill new room
            if (!template.prefillTemplate().equals(RoomTemplate.NO_TEMPLATE)) {
                CompactStructureGenerator.fillWithTemplate(compactDim, template.prefillTemplate(), template.dimensions(), newRoom.center());
            }

            machine.setConnectedRoom(newRoom);

            PlayerUtil.teleportPlayerIntoRoom(server, owner, newRoom);

            // Mark the player as inside the machine, set external spawn, and yeet
            owner.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                var entry = PreciseDimensionalPosition.fromPlayer(owner);
                hist.addHistory(new PlayerRoomHistoryItem(entry, machine.getLevelPosition()));
            });
        } catch (MissingDimensionException | NonexistentRoomException e) {
            CompactMachines.LOGGER.error("Error occurred while generating new room and machine info for first player entry.", e);
        }
    }

    public static float destroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        CompactMachineBlockEntity tile = (CompactMachineBlockEntity) worldIn.getBlockEntity(pos);
        float normalHardness = state.getDestroyProgress(player, worldIn, pos);

        if (tile == null)
            return normalHardness;

        boolean hasPlayers = tile.hasPlayersInside();


        // If there are players inside, check config for break handling
        if (hasPlayers) {
            EnumMachinePlayersBreakHandling hand = ServerConfig.MACHINE_PLAYER_BREAK_HANDLING.get();
            switch (hand) {
                case UNBREAKABLE:
                    return 0;

                case OWNER:
                    Optional<UUID> ownerUUID = tile.getOwnerUUID();
                    return ownerUUID
                            .map(uuid -> player.getUUID() == uuid ? normalHardness : 0)
                            .orElse(normalHardness);

                case ANYONE:
                    return normalHardness;
            }
        }

        // No players inside - let anyone break it
        return normalHardness;
    }

    public static ItemStack getCloneItemStack(BlockGetter world, BlockState state, BlockPos pos) {
        if(state.is(LegacySizedCompactMachineBlock.LEGACY_MACHINES_TAG) && state.getBlock() instanceof LegacySizedCompactMachineBlock l)
        {
            final var item = LegacyCompactMachineItem.getItemBySize(l.getSize());
            return new ItemStack(item);
        }

        // If not a machine or the block data is invalid...
        if(!state.is(CMTags.MACHINE_BLOCK) || !(world.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile))
            return UnboundCompactMachineItem.unbound();

        return tile.basicRoomInfo()
                .map(BoundCompactMachineItem::createForRoom)
                .orElse(UnboundCompactMachineItem.unbound());
    }

}
