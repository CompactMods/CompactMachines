package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RedstoneInTunnelItem extends TunnelItem {
    public RedstoneInTunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public TunnelDefinition getDefinition() {
        return Registration.REDSTONE_IN_TUNNEL.get();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        Item redstoneOut = Registration.ITEM_REDSTONEOUT_TUNNEL.get();
        return super.swapTunnelType(redstoneOut, playerIn, handIn);
    }
}
