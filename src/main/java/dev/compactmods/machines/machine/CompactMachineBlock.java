package dev.compactmods.machines.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.rooms.RoomSize;
import dev.compactmods.machines.data.NbtConstants;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CompactMachineBlock extends Block implements EntityBlock {

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
            ServerLevel compactWorld = serverWorld.getServer().getLevel(Registration.COMPACT_DIMENSION);
            if (compactWorld == null) {
                CompactMachines.LOGGER.warn("Warning: Compact Dimension was null! Cannot fetch internal state for machine neighbor change listener.");
            }

            // TODO - Send notification to level tunnel listeners (API)
        }
    }

    public static Block getBySize(RoomSize size) {
        return switch (size) {
            case TINY -> Registration.MACHINE_BLOCK_TINY.get();
            case SMALL -> Registration.MACHINE_BLOCK_SMALL.get();
            case NORMAL -> Registration.MACHINE_BLOCK_NORMAL.get();
            case LARGE -> Registration.MACHINE_BLOCK_LARGE.get();
            case GIANT -> Registration.MACHINE_BLOCK_GIANT.get();
            case MAXIMUM -> Registration.MACHINE_BLOCK_MAXIMUM.get();
        };

    }

    public static Item getItemBySize(RoomSize size) {
        return switch (size) {
            case TINY -> Registration.MACHINE_BLOCK_ITEM_TINY.get();
            case SMALL -> Registration.MACHINE_BLOCK_ITEM_SMALL.get();
            case NORMAL -> Registration.MACHINE_BLOCK_ITEM_NORMAL.get();
            case LARGE -> Registration.MACHINE_BLOCK_ITEM_LARGE.get();
            case GIANT -> Registration.MACHINE_BLOCK_ITEM_GIANT.get();
            case MAXIMUM -> Registration.MACHINE_BLOCK_ITEM_MAXIMUM.get();
        };
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        Block given = getBySize(this.size);
        ItemStack stack = new ItemStack(given, 1);

        CompoundTag nbt = stack.getOrCreateTag();
        // nbt.putString("size", this.size.getName());

        CompactMachineBlockEntity tileEntity = (CompactMachineBlockEntity) world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity.mapped()) {
            nbt.putInt(NbtConstants.MACHINE_ID, tileEntity.machineId);
        }

        return stack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        if (worldIn.isClientSide())
            return;

        if (worldIn.getBlockEntity(pos) instanceof CompactMachineBlockEntity tile) {
            // The machine already has data for some reason
            if (tile.machineId != -1)
                return;

            // TODO - Custom machine names

            if (!stack.hasTag())
                return;

            CompoundTag nbt = stack.getTag();
            if (nbt == null)
                return;

            if (nbt.contains(NbtConstants.MACHINE_ID)) {
                int machineID = nbt.getInt(NbtConstants.MACHINE_ID);
                tile.setMachineId(machineID);
            }

            tile.doPostPlaced();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        // TODO - Open GUI with machine preview
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.isEmpty())
            return InteractionResult.PASS;

        // TODO - Item tags instead of direct item reference here
        if (mainItem.getItem() == Registration.PERSONAL_SHRINKING_DEVICE.get()) {
            // Try teleport to compact machine dimension
            PlayerUtil.teleportPlayerIntoMachine(level, player, pos, size);
        }

        return InteractionResult.SUCCESS;
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
        if(level.isClientSide || server == null) {
            super.onRemove(oldState, level, pos, newState, a);
            return;
        }

        if(level.getBlockEntity(pos) instanceof CompactMachineBlockEntity entity) {
            if(entity.mapped()) {
                var machines = CompactMachineData.get(server);
                machines.remove(entity.machineId);
            }
        }

        super.onRemove(oldState, level, pos, newState, a);
    }
}
