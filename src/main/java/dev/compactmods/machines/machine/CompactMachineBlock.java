package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.dimension.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.RoomCapabilities;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.upgrade.RoomUpgradeItem;
import dev.compactmods.machines.upgrade.RoomUpgradeManager;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

public class CompactMachineBlock extends Block implements EntityBlock {

    public static final TagKey<Block> TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(Constants.MOD_ID, "machine"));

    private final RoomSize size;

    public CompactMachineBlock(RoomSize size, BlockBehaviour.Properties props) {
        super(props);
        this.size = size;
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        CompactMachineBlockEntity tile = (CompactMachineBlockEntity) worldIn.getBlockEntity(pos);
        float normalHardness = super.getDestroyProgress(state, player, worldIn, pos);

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

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        // TODO Redstone out tunnels
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, changedBlock, changedPos, isMoving);

        if (world.isClientSide)
            return;

        ServerLevel serverWorld = (ServerLevel) world;

        if (serverWorld.getBlockEntity(pos) instanceof CompactMachineBlockEntity machine) {
            ServerLevel compactWorld = serverWorld.getServer().getLevel(CompactDimension.LEVEL_KEY);
            if (compactWorld == null) {
                CompactMachines.LOGGER.warn("Warning: Compact Dimension was null! Cannot fetch internal state for machine neighbor change listener.");
            }

            // TODO - Send notification to dimension tunnel listeners (API)
        }
    }

    public static Block getBySize(RoomSize size) {
        return switch (size) {
            case TINY -> Machines.MACHINE_BLOCK_TINY.get();
            case SMALL -> Machines.MACHINE_BLOCK_SMALL.get();
            case NORMAL -> Machines.MACHINE_BLOCK_NORMAL.get();
            case LARGE -> Machines.MACHINE_BLOCK_LARGE.get();
            case GIANT -> Machines.MACHINE_BLOCK_GIANT.get();
            case MAXIMUM -> Machines.MACHINE_BLOCK_MAXIMUM.get();
        };

    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        Block given = getBySize(this.size);
        ItemStack stack = new ItemStack(given, 1);

        if (world.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
            tile.getConnectedRoom().ifPresent(room -> {
                CompactMachineItem.setRoom(stack, room);
            });
        }

        return stack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        if (worldIn.isClientSide())
            return;

        if (worldIn.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile && worldIn instanceof ServerLevel sl) {
            // TODO - Custom machine names
            if (!stack.hasTag())
                return;

            CompoundTag nbt = stack.getTag();
            if (nbt == null)
                return;

            // Machine was previously bound to a room - make a new binding post-place
            CompactMachineItem.getRoom(stack).ifPresent(room -> {
                final var g = DimensionMachineGraph.forDimension(sl);
                g.connectMachineToRoom(pos, room);
                tile.syncConnectedRoom();
            });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        MinecraftServer server = level.getServer();
        ItemStack mainItem = player.getMainHandItem();

        if (mainItem.isEmpty() && level.getBlockEntity(pos) instanceof CompactMachineBlockEntity machine) {
            if (state.getBlock() instanceof CompactMachineBlock cmBlock) {
                machine.getConnectedRoom().ifPresent(room -> {
                    var size = cmBlock.getSize();
                    try {
                        final var roomName = Rooms.getRoomName(server, room);
                        NetworkHooks.openScreen((ServerPlayer) player, MachineRoomMenu.makeProvider(server, room, machine.getLevelPosition()), (buf) -> {
                            buf.writeBlockPos(pos);
                            buf.writeWithCodec(LevelBlockPosition.CODEC, machine.getLevelPosition());
                            buf.writeChunkPos(room);
                            roomName.ifPresentOrElse(name -> {
                                buf.writeBoolean(true);
                                buf.writeUtf(name);
                            }, () -> {
                                buf.writeBoolean(false);
                            });
                        });
                    } catch (NonexistentRoomException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        // TODO - Item tags instead of direct item reference here
        if (mainItem.getItem() == Shrinking.PERSONAL_SHRINKING_DEVICE.get()) {
            // Try teleport to compact machine dimension
            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
                tile.getConnectedRoom().ifPresentOrElse(room -> {
                    try {
                        PlayerUtil.teleportPlayerIntoMachine(level, player, pos);
                    } catch (MissingDimensionException e) {
                        e.printStackTrace();
                    }
                }, () -> createAndEnterRoom(player, server, tile));
            }
        }

        // Try and pull the name off the nametag and apply it to the room
        if (mainItem.getItem() instanceof NameTagItem && mainItem.hasCustomHoverName()) {
            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
                tile.getConnectedRoom().ifPresentOrElse(room -> {
                    Rooms.getOwner(server, room).ifPresent(profile -> {
                        try {
                            if (player.getUUID().equals(profile.getId())) {
                                final var newName = mainItem.getHoverName().getString(120);
                                Rooms.updateName(server, room, newName);
                            } else {
                                player.displayClientMessage(TranslationUtil.message(Messages.CANNOT_RENAME_NOT_OWNER, profile.getName()), true);
                            }
                        } catch (NonexistentRoomException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }, () -> {
                    CompactMachines.LOGGER.warn("Tried to apply upgrade to a 'claimed' machine, but there was no owner data attached.");
                });
            }
        }

        // Upgrade Item
        if (mainItem.is(CMTags.ROOM_UPGRADE_ITEM)) {
            final var reg = MachineRoomUpgrades.REGISTRY.get();
            if (mainItem.getItem() instanceof RoomUpgradeItem upItem) {
                if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
                    tile.getConnectedRoom().ifPresent(room -> {
                        Rooms.getOwner(server, room).ifPresent(prof -> {
                            if (!player.getUUID().equals(prof.getId())) {
                                player.displayClientMessage(TranslationUtil.message(Messages.NOT_ROOM_OWNER, prof.getName()), true);
                                return;
                            }

                            final var upg = upItem.getUpgradeType();
                            final var manager = RoomUpgradeManager.get(server.getLevel(CompactDimension.LEVEL_KEY));

                            if (manager.hasUpgrade(room, upg)) {
                                player.displayClientMessage(TranslationUtil.message(Messages.ALREADY_HAS_UPGRADE), true);
                            } else {
                                final var added = manager.addUpgrade(upg, room);

                                if (added) {
                                    player.displayClientMessage(TranslationUtil.message(Messages.UPGRADE_APPLIED)
                                            .withStyle(ChatFormatting.DARK_GREEN), true);
                                } else {
                                    player.displayClientMessage(TranslationUtil.message(Messages.UPGRADE_ADD_FAILED)
                                            .withStyle(ChatFormatting.DARK_RED), true);
                                }
                            }
                        });
                    });
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void createAndEnterRoom(Player player, MinecraftServer server, CompactMachineBlockEntity tile) {
        try {
            final var newRoomPos = Rooms.createNew(server, size, player.getUUID());
            tile.setConnectedRoom(newRoomPos);

            PlayerUtil.teleportPlayerIntoRoom(server, player, newRoomPos, true);

            // Mark the player as inside the machine, set external spawn, and yeet
            player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                var entry = PreciseDimensionalPosition.fromPlayer(player);
                hist.addHistory(new PlayerRoomHistoryItem(entry, tile.getLevelPosition()));
            });
        } catch (MissingDimensionException | NonexistentRoomException e) {
            CompactMachines.LOGGER.error("Error occurred while generating new room and machine info for first player entry.", e);
        }
    }

    public RoomSize getSize() {
        return this.size;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompactMachineBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean a) {
        MinecraftServer server = level.getServer();
        if (level.isClientSide || server == null) {
            super.onRemove(oldState, level, pos, newState, a);
            return;
        }

        if (level instanceof ServerLevel sl) {
            final var serv = sl.getServer();
            final var compactDim = serv.getLevel(CompactDimension.LEVEL_KEY);

            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity entity) {
                entity.getConnectedRoom().ifPresent(room -> {
                    final var dimGraph = DimensionMachineGraph.forDimension(sl);
                    dimGraph.disconnect(pos);

                    if (compactDim == null)
                        return;

                    final var tunnels = TunnelConnectionGraph.forRoom(compactDim, room);
                    tunnels.unregister(pos);
                });
            }
        }

        super.onRemove(oldState, level, pos, newState, a);
    }
}
