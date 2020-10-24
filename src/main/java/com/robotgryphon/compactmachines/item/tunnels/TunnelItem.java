package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.tunnels.TunnelRegistration;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public abstract class TunnelItem extends Item {
    public TunnelItem(Properties properties) {
        super(properties);
    }

    public abstract TunnelRegistration getDefinition();

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World w = context.getWorld();
        if (w.isRemote())
            return ActionResultType.SUCCESS;

        BlockPos pos = context.getPos();
        BlockState blockState = w.getBlockState(pos);

        if (blockState.getBlock() != Registrations.BLOCK_SOLID_WALL.get())
            return ActionResultType.FAIL;

        if (context.getPlayer() instanceof ServerPlayerEntity) {
            ItemStack is = context.getItem();
            Item i = is.getItem();

            TunnelItem ti = ((TunnelItem) i);
            TunnelRegistration definition = ti.getDefinition();

            BlockState defaultSolidItem = Registrations.BLOCK_TUNNEL_WALL.get()
                    .getDefaultState()
                    .with(TunnelWallBlock.TUNNEL_TYPE, definition.getType());

            w.setBlockState(pos, defaultSolidItem);
            is.shrink(1);
            return ActionResultType.CONSUME;
        }

        return ActionResultType.FAIL;
    }

    /**
     * Implementation for easily swapping a tunnel type for another.
     * @param type
     * @param player
     * @param hand
     * @return
     */
    protected ActionResult<ItemStack> swapTunnelType(Item type, PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            if (player.isSneaking()) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                ItemStack stack = serverPlayer.getHeldItem(hand);
                ItemStack newStack = new ItemStack(type, stack.getCount());

                serverPlayer.setHeldItem(hand, newStack);

                IFormattableTextComponent msg = new StringTextComponent("switch state")
                        .mergeStyle(TextFormatting.GOLD);

                serverPlayer.sendStatusMessage(msg, true);
                return ActionResult.resultConsume(newStack);
            }
        }

        return ActionResult.resultPass(player.getHeldItem(hand));
    }
}
