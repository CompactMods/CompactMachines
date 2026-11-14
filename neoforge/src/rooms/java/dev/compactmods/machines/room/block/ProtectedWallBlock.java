package dev.compactmods.machines.room.block;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Supplier;

public abstract class ProtectedWallBlock extends Block {

    public static final String UNBREAKABLE_BLOCK = Util.makeDescriptionId("messages", CompactMachinesCore.identifier("solid_wall"));

    static final Supplier<Component> COMPONENT_UNBREAKABLE_BLOCK = () -> Component.translatableWithFallback(UNBREAKABLE_BLOCK, "Warning! Unbreakable for non-creative players!")
            .withStyle(ChatFormatting.DARK_RED);

    protected ProtectedWallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        if(!canPlayerBreak(player))
            return false;

        level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide() ? 11 : 3);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return false;
    }

    public boolean canPlayerBreak(Player player) {
        if(!player.isCreative()) return false;
        if(!player.isShiftKeyDown()) return false;

        return true;
    }
}
