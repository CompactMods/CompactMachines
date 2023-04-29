package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.Registries;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.forge.room.Rooms;
import dev.compactmods.machines.forge.room.ui.MachineRoomMenu;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.forge.upgrade.RoomUpgradeItem;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelMachineFilters;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelTypeFilters;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
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
                        graph.positions(TunnelMachineFilters.sided(machine.getLevelPosition(), dir), TunnelTypeFilters.redstone(Tunnels::getDefinition)).forEach(tunnelPos -> {
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
                g.register(pos, room);
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
        if (mainItem.getItem() instanceof NameTagItem && mainItem.hasCustomHoverName()) {
            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
                final var ownerProfile = tile.getOwnerUUID().flatMap(id -> PlayerUtil.getProfileByUUID(server, id));
                boolean isOwner = ownerProfile.map(p -> p.getId().equals(player.getUUID())).orElse(false);
                boolean isOp = player.hasPermissions(Commands.LEVEL_MODERATORS);

                if (ownerProfile.isEmpty()) {
                    return InteractionResult.FAIL;
                }

                if (!isOp || !isOwner)
                    return InteractionResult.FAIL;
                else {
                    ownerProfile.ifPresent(owner -> {
                        player.displayClientMessage(TranslationUtil.message(Messages.CANNOT_RENAME_NOT_OWNER,
                                owner.getName()), true);
                    });
                }

                tile.connectedRoom().ifPresent(roomCode -> {
                    try {
                        final var newName = mainItem.getHoverName().getString(120);
                        Rooms.updateName(server, roomCode, newName);
                    } catch (NonexistentRoomException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        // Upgrade Item
        if (mainItem.is(CMTags.ROOM_UPGRADE_ITEM)) {
            final var reg = MachineRoomUpgrades.REGISTRY.get();
            if (mainItem.getItem() instanceof RoomUpgradeItem upItem) {
                if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
                    // TODO
//                        tile.roomInfo().ifPresent(room -> {
//                            final var ownerId = room.owner(roomData);
//                            if (!player.getUUID().equals(ownerId)) {
//                                final var ownerName = server.getPlayerList().getPlayer(ownerId).getName();
//                                player.displayClientMessage(TranslationUtil.message(Messages.NOT_ROOM_OWNER, ownerName), true);
//                                return;
//                            }
//
//                            final var upg = RoomUpgradeHelper.getUpgradeId(mainItem);
//                            final var manager = RoomUpgradeManager.get(compactDim);
//
//                            if (manager.hasUpgrade(room.code(), upg)) {
//                                player.displayClientMessage(TranslationUtil.message(Messages.ALREADY_HAS_UPGRADE), true);
//                            } else {
//                                final var added = manager.addUpgrade(upg, room.code());
//
//                                if (added) {
//                                    player.displayClientMessage(TranslationUtil.message(Messages.UPGRADE_APPLIED)
//                                            .withStyle(ChatFormatting.DARK_GREEN), true);
//                                } else {
//                                    player.displayClientMessage(TranslationUtil.message(Messages.UPGRADE_ADD_FAILED)
//                                            .withStyle(ChatFormatting.DARK_RED), true);
//                                }
//                            }
//                        });
                }
            }
        }


        // All other items, open preview screen
        if (level.getBlockEntity(pos) instanceof
                CompactMachineBlockEntity machine) {
            if (state.getBlock() instanceof LegacySizedCompactMachineBlock cmBlock) {
                machine.connectedRoom().ifPresent(roomCode -> {
                    var size = cmBlock.getSize();
                    try {
                        final var roomName = Rooms.getRoomName(server, roomCode);
                        NetworkHooks.openScreen((ServerPlayer) player, MachineRoomMenu.makeProvider(server, roomCode, machine.getLevelPosition()), (buf) -> {
                            buf.writeBlockPos(pos);
                            buf.writeWithCodec(GlobalPos.CODEC, machine.getLevelPosition());
                            buf.writeUtf(roomCode);
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

        MachineBlockUtil.cleanupTunnelsPostMachineRemove(level, pos);

        super.onRemove(oldState, level, pos, newState, a);
    }
}
