package dev.compactmods.machines.block;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.block.tiles.CompactMachineTile;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.reference.Reference;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class BlockCompactMachine extends Block implements EntityBlock {

    private final EnumMachineSize size;

    public BlockCompactMachine(EnumMachineSize size, BlockBehaviour.Properties props) {
        super(props);
        this.size = size;
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        CompactMachineTile tile = (CompactMachineTile) worldIn.getBlockEntity(pos);
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

        if (serverWorld.getBlockEntity(pos) instanceof CompactMachineTile machine) {
            ServerLevel compactWorld = serverWorld.getServer().getLevel(Registration.COMPACT_DIMENSION);
            if (compactWorld == null) {
                CompactMachines.LOGGER.warn("Warning: Compact Dimension was null! Cannot fetch internal state for machine neighbor change listener.");
            }

            // TODO - Send notification to level tunnel listeners (API)
        }
    }

    public static Block getBySize(EnumMachineSize size) {
        return switch (size) {
            case TINY -> Registration.MACHINE_BLOCK_TINY.get();
            case SMALL -> Registration.MACHINE_BLOCK_SMALL.get();
            case NORMAL -> Registration.MACHINE_BLOCK_NORMAL.get();
            case LARGE -> Registration.MACHINE_BLOCK_LARGE.get();
            case GIANT -> Registration.MACHINE_BLOCK_GIANT.get();
            case MAXIMUM -> Registration.MACHINE_BLOCK_MAXIMUM.get();
        };

    }

    public static Item getItemBySize(EnumMachineSize size) {
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

        CompactMachineTile tileEntity = (CompactMachineTile) world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity.mapped()) {
            nbt.putInt(Reference.CompactMachines.NBT_MACHINE_ID, tileEntity.machineId);
        }

        return stack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        if (worldIn.isClientSide())
            return;

        if (worldIn.getBlockEntity(pos) instanceof CompactMachineTile tile) {
            // The machine already has data for some reason
            if (tile.machineId != -1)
                return;

            // TODO - Custom machine names

            if (!stack.hasTag())
                return;

            CompoundTag nbt = stack.getTag();
            if (nbt == null)
                return;

            if (nbt.contains("cm")) {
                CompoundTag machineData = nbt.getCompound("cm");
                if (machineData.contains(Reference.CompactMachines.NBT_MACHINE_ID)) {
                    int machineID = machineData.getInt(Reference.CompactMachines.NBT_MACHINE_ID);
                    tile.setMachineId(machineID);
                }
            }

            tile.doPostPlaced();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide())
            return InteractionResult.SUCCESS;

        // TODO - Open GUI with machine preview
        if (player instanceof ServerPlayer serverPlayer) {

            ItemStack mainItem = player.getMainHandItem();
            if (mainItem.isEmpty())
                return InteractionResult.PASS;

            // TODO - Item tags instead of direct item reference here
            if (mainItem.getItem() == Registration.PERSONAL_SHRINKING_DEVICE.get()) {
                // Try teleport to compact machine dimension
                PlayerUtil.teleportPlayerIntoMachine(serverPlayer, pos, size);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public EnumMachineSize getSize() {
        return this.size;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompactMachineTile(pos, state);
    }
}
