package dev.compactmods.machines.compat.jade.providers.server;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TunnelComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final TunnelComponentProvider INSTANCE = new TunnelComponentProvider();

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        final ServerLevel compactLevel = (ServerLevel) accessor.getLevel();

        final TunnelWallEntity tunnelWallEntity = (TunnelWallEntity) accessor.getBlockEntity();
        final IDimensionalBlockPosition machinePosition = tunnelWallEntity.getConnectedPosition();
        final IDimensionalPosition connectedWorldPosition = machinePosition.relative(tunnelWallEntity.getConnectedSide());

        final BlockState connectedBlockState = connectedWorldPosition
                .level(compactLevel.getServer())
                .getBlockState(connectedWorldPosition.getBlockPosition());

        final BlockEntity connectedBlockEntity = connectedWorldPosition
                .level(compactLevel.getServer())
                .getBlockEntity(connectedWorldPosition.getBlockPosition());

        if (!connectedBlockState.isAir()) {
            ItemStack connectedBlockItemStack = connectedBlockState.getBlock().asItem().getDefaultInstance();
            data.put("connected_block", connectedBlockItemStack.save(new CompoundTag()));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Constants.MOD_ID, "tunnel_connection");
    }
}
