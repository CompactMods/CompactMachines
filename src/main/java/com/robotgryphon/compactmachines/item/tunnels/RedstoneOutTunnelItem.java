package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RedstoneOutTunnelItem extends TunnelItem {

    public RedstoneOutTunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public TunnelDefinition getDefinition() {
        return Registration.REDSTONE_OUT_TUNNEL.get();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        Item redstoneIn = Registration.ITEM_REDSTONEIN_TUNNEL.get();
        return super.swapTunnelType(redstoneIn, playerIn, handIn);
    }
}
