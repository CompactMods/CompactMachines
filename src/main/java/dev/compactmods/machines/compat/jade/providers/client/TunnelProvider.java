package dev.compactmods.machines.compat.jade.providers.client;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.tunnel.TunnelWallBlock;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public class TunnelProvider implements IBlockComponentProvider {
    public static final TunnelProvider INSTANCE = new TunnelProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Direction outSide = blockAccessor.getBlockState().getValue(TunnelWallBlock.CONNECTED_SIDE);

        IElementHelper helper = iTooltip.getElementHelper();
        IElement compassIcon = helper
                .item(new ItemStack(Items.COMPASS), 1)
                .size(new Vec2(15, 15)).translate(new Vec2(0, -1));
        iTooltip.add(compassIcon);

        String sideTranslated = Constants.MOD_ID + ".direction." + outSide.getName();
        IElement directionText = helper
                .text(Component.translatable(sideTranslated))
                .translate(new Vec2(5, 3));
        iTooltip.append(directionText);

        if (blockAccessor.getServerData().contains("connected_block")) {
            ItemStack connectedBlockItemStack =
                    ItemStack.of((CompoundTag) blockAccessor.getServerData().get("connected_block"));
            IElement itemIcon = helper
                    .item(connectedBlockItemStack, 1)
                    .size(new Vec2(15, 15)).translate(new Vec2(0, -1));
            iTooltip.add(itemIcon);
            IElement itemName = helper
                    .text(connectedBlockItemStack.getHoverName())
                    .translate(new Vec2(5, 3));
            iTooltip.append(itemName);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Constants.MOD_ID, "tunnel");
    }
}
