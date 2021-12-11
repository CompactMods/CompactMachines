package dev.compactmods.machines.block;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.block.tiles.CompactMachineTile;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.reference.EnumMachineSize;
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
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
//        TODO Redstone out tunnels
//        if(!(blockAccess.getTileEntity(pos) instanceof TileEntityMachine)) {
//            return 0;
//        }
//
//        TileEntityMachine machine = (TileEntityMachine) blockAccess.getTileEntity(pos);
//        if(machine.isInsideItself()) {
//            return 0;
//        }
//
//        return machine.getRedstonePowerOutput(side.getOpposite());
        return 0;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, changedBlock, changedPos, isMoving);

        if (world.isClientSide)
            return;

        ServerLevel serverWorld = (ServerLevel) world;

        BlockState changedState = serverWorld.getBlockState(changedPos);

        CompactMachineTile machine = (CompactMachineTile) serverWorld.getBlockEntity(pos);
        if (machine == null)
            return;

        ServerLevel compactWorld = serverWorld.getServer().getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.warn("Warning: Compact Dimension was null! Cannot fetch internal state for machine neighbor change listener.");
            return;
        }
    }


    public static EnumMachineSize getMachineSizeFromNBT(@Nullable CompoundTag tag) {
        try {
            if (tag == null)
                return EnumMachineSize.TINY;

            if (!tag.contains("size"))
                return EnumMachineSize.TINY;

            String sizeFromTag = tag.getString("size");
            return EnumMachineSize.getFromSize(sizeFromTag);
        } catch (Exception ex) {
            return EnumMachineSize.TINY;
        }
    }

    public static Block getBySize(EnumMachineSize size) {
        switch (size) {
            case TINY:
                return Registration.MACHINE_BLOCK_TINY.get();

            case SMALL:
                return Registration.MACHINE_BLOCK_SMALL.get();

            case NORMAL:
                return Registration.MACHINE_BLOCK_NORMAL.get();

            case LARGE:
                return Registration.MACHINE_BLOCK_LARGE.get();

            case GIANT:
                return Registration.MACHINE_BLOCK_GIANT.get();

            case MAXIMUM:
                return Registration.MACHINE_BLOCK_MAXIMUM.get();
        }

        return Registration.MACHINE_BLOCK_NORMAL.get();
    }

    public static Item getItemBySize(EnumMachineSize size) {
        switch (size) {
            case TINY:
                return Registration.MACHINE_BLOCK_ITEM_TINY.get();

            case SMALL:
                return Registration.MACHINE_BLOCK_ITEM_SMALL.get();

            case NORMAL:
                return Registration.MACHINE_BLOCK_ITEM_NORMAL.get();

            case LARGE:
                return Registration.MACHINE_BLOCK_ITEM_LARGE.get();

            case GIANT:
                return Registration.MACHINE_BLOCK_ITEM_GIANT.get();

            case MAXIMUM:
                return Registration.MACHINE_BLOCK_ITEM_MAXIMUM.get();
        }

        return Registration.MACHINE_BLOCK_ITEM_NORMAL.get();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        Block given = getBySize(this.size);
        ItemStack stack = new ItemStack(given, 1);

        CompoundTag nbt = stack.getOrCreateTagElement("cm");
        nbt.putString("size", this.size.getName());

        CompactMachineTile tileEntity = (CompactMachineTile) world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity.mapped()) {
            nbt.putInt("coords", tileEntity.machineId);
        }

        return stack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        if (worldIn.isClientSide())
            return;

        ServerLevel serverWorld = (ServerLevel) worldIn;

        boolean hasProperTile = worldIn.getBlockEntity(pos) instanceof CompactMachineTile;
        if (!hasProperTile)
            return;

        CompactMachineTile tile = (CompactMachineTile) worldIn.getBlockEntity(pos);

        // The machine already has data for some reason
        if (tile.machineId != -1)
            return;

//        if (stack.hasDisplayName()) {
//            tile.setCustomName(stack.getDisplayName());
//        }

        if(!stack.hasTag())
            return;

        CompoundTag nbt = stack.getTag();
        if(nbt == null)
            return;

        if (nbt.contains("cm")) {
            CompoundTag machineData = nbt.getCompound("cm");
            if (machineData.contains("coords")) {
                int machineID = machineData.getInt("coords");
                tile.setMachineId(machineID);
            }
        }

        tile.doPostPlaced();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide())
            return InteractionResult.SUCCESS;

        // TODO - Open GUI with machine preview
        if (player instanceof ServerPlayer) {

            ServerPlayer serverPlayer = (ServerPlayer) player;

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
