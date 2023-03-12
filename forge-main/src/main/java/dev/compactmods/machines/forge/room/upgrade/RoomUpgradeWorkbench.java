package dev.compactmods.machines.forge.room.upgrade;

import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoomUpgradeWorkbench extends Block implements EntityBlock {
    public RoomUpgradeWorkbench(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TranslationUtil.tooltip(Tooltips.NOT_YET_IMPLEMENTED).withStyle(ChatFormatting.DARK_RED));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RoomUpgradeWorkbenchEntity(pPos, pState);
    }
}
