package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.Registries;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.room.ui.MachineRoomMenu;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.upgrade.RoomUpgradeItem;
import dev.compactmods.machines.room.upgrade.RoomUpgradeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "5.2.0")
public class LegacySizedCompactMachineBlock extends Block implements EntityBlock {

    public static final TagKey<Block> LEGACY_MACHINES_TAG = TagKey.create(Registries.BLOCKS.getRegistryKey(),
            new ResourceLocation(Constants.MOD_ID, "legacy_machines"));

    private final RoomSize size;

    public LegacySizedCompactMachineBlock(RoomSize size, BlockBehaviour.Properties props) {
        super(props);
        this.size = size;
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        return MachineBlockUtil.destroyProgress(state, player, worldIn, pos);
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
        if (world.isClientSide)
            return;

        ServerLevel serverWorld = (ServerLevel) world;
        if (serverWorld.getBlockEntity(pos) instanceof CompactMachineBlockEntity machine) {
            ServerLevel compactWorld = serverWorld.getServer().getLevel(CompactDimension.LEVEL_KEY);
            if (compactWorld == null) {
                CompactMachines.LOGGER.warn("Warning: Compact Dimension was null! Cannot fetch internal state for machine neighbor change listener.");
            } else {
                for (final var dir : Direction.Plane.HORIZONTAL) {
                    if (!pos.relative(dir).equals(changedPos)) continue;

                    // Horizontal neighbor changed
                    machine.getTunnelGraph().ifPresent(graph -> {
                        // Update redstone tunnel signals
                        graph.getRedstoneTunnels(machine.getLevelPosition(), dir).forEach(tunnelPos -> {
                            compactWorld.updateNeighbourForOutputSignal(tunnelPos, Tunnels.BLOCK_TUNNEL_WALL.get());
                        });
                    });
                }
            }
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
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return MachineBlockUtil.getCloneItemStack(level, state, pos);
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
            BoundCompactMachineItem.getRoom(stack).ifPresent(room -> {
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
        if (mainItem.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            return MachineBlockUtil.tryRoomTeleport(level, pos, sp, server);
        }

        // Try and pull the name off the nametag and apply it to the room
        try {
            var compactDim = CompactDimension.forServer(server);
            final var roomData = CompactRoomProvider.instance(compactDim);

            if (mainItem.getItem() instanceof NameTagItem && mainItem.hasCustomHoverName()) {
                if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
                    tile.roomInfo().ifPresentOrElse(room -> {
                        final var ownerId = room.owner(roomData);
                        try {
                            if (player.getUUID().equals(ownerId)) {
                                final var newName = mainItem.getHoverName().getString(120);
                                Rooms.updateName(server, room.code(), newName);
                            } else {
                                player.displayClientMessage(TranslationUtil.message(Messages.CANNOT_RENAME_NOT_OWNER,
                                        server.getPlayerList().getPlayer(ownerId).getName()), true);
                            }
                        } catch (NonexistentRoomException e) {
                            throw new RuntimeException(e);
                        }
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
                        tile.roomInfo().ifPresent(room -> {
                            final var ownerId = room.owner(roomData);
                            if (!player.getUUID().equals(ownerId)) {
                                final var ownerName = server.getPlayerList().getPlayer(ownerId).getName();
                                player.displayClientMessage(TranslationUtil.message(Messages.NOT_ROOM_OWNER, ownerName), true);
                                return;
                            }

                            final var upg = upItem.getUpgradeType();
                            final var manager = RoomUpgradeManager.get(compactDim);

                            if (manager.hasUpgrade(room.code(), upg)) {
                                player.displayClientMessage(TranslationUtil.message(Messages.ALREADY_HAS_UPGRADE), true);
                            } else {
                                final var added = manager.addUpgrade(upg, room.code());

                                if (added) {
                                    player.displayClientMessage(TranslationUtil.message(Messages.UPGRADE_APPLIED)
                                            .withStyle(ChatFormatting.DARK_GREEN), true);
                                } else {
                                    player.displayClientMessage(TranslationUtil.message(Messages.UPGRADE_ADD_FAILED)
                                            .withStyle(ChatFormatting.DARK_RED), true);
                                }
                            }
                        });
                    }
                }
            }

            // All other items, open preview screen
            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity machine) {
                if (state.getBlock() instanceof LegacySizedCompactMachineBlock cmBlock) {
                    machine.roomInfo().ifPresent(room -> {
                        var size = cmBlock.getSize();
                        try {
                            final var roomName = Rooms.getRoomName(server, room.code());
                            NetworkHooks.openScreen((ServerPlayer) player, MachineRoomMenu.makeProvider(server, room, machine.getLevelPosition()), (buf) -> {
                                buf.writeBlockPos(pos);
                                buf.writeWithCodec(LevelBlockPosition.CODEC, machine.getLevelPosition());
                                buf.writeUtf(room.code());
                                roomName.ifPresentOrElse(name -> {
                                    buf.writeBoolean(true);
                                    buf.writeUtf(name);
                                }, () -> {
                                    buf.writeBoolean(false);
                                    buf.writeUtf("");
                                });
                            });
                        } catch (NonexistentRoomException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static RoomTemplate getLegacyTemplate(RoomSize size) {
        final var temp = switch (size) {
            case TINY -> LegacySizedTemplates.EMPTY_TINY;
            case SMALL -> LegacySizedTemplates.EMPTY_SMALL;
            case NORMAL -> LegacySizedTemplates.EMPTY_NORMAL;
            case LARGE -> LegacySizedTemplates.EMPTY_LARGE;
            case GIANT -> LegacySizedTemplates.EMPTY_GIANT;
            case MAXIMUM -> LegacySizedTemplates.EMPTY_COLOSSAL;
        };

        return temp.template();
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
                entity.roomInfo().ifPresent(room -> {
                    final var dimGraph = DimensionMachineGraph.forDimension(sl);
                    dimGraph.disconnect(pos);

                    if (compactDim == null)
                        return;

                    final var tunnels = TunnelConnectionGraph.forRoom(compactDim, room.code());
                    tunnels.unregister(pos);
                });
            }
        }

        super.onRemove(oldState, level, pos, newState, a);
    }
}
