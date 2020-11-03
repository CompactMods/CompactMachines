package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.tunnels.api.IRedstoneTunnel;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class TunnelItem extends Item {
    public TunnelItem(Properties properties) {
        super(properties);
    }

    public abstract TunnelDefinition getDefinition();

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
            TunnelDefinition definition = ti.getDefinition();

            BlockState tunnelState = Registrations.BLOCK_TUNNEL_WALL.get()
                    .getDefaultState();

            // Redstone Support
            boolean redstone = (definition instanceof IRedstoneTunnel);
            tunnelState = tunnelState.with(TunnelWallBlock.REDSTONE, redstone);
            w.setBlockState(pos, tunnelState, 3);

            // Get the server and add a deferred task - allows the tile to be created on the client first
            MinecraftServer server = ((ServerWorld) context.getWorld()).getServer();
            server.deferTask(() -> {
                TunnelWallTile tile = (TunnelWallTile) context.getWorld().getTileEntity(context.getPos());
                tile.setTunnelType(definition.getRegistryName());
            });

            is.shrink(1);
            return ActionResultType.CONSUME;
        }

        return ActionResultType.FAIL;
    }

    /**
     * Implementation for easily swapping a tunnel type for another.
     *
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
