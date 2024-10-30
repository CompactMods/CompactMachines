package dev.compactmods.machines.compat.jade.providers.server;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.TunnelItem;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CompactMachineComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    public static final CompactMachineComponentProvider INSTANCE = new CompactMachineComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        final CompactMachineBlockEntity machine = (CompactMachineBlockEntity) accessor.getBlockEntity();
        machine.getConnectedRoom().ifPresent(room -> {
            final var compactDim = accessor.getLevel().getServer().getLevel(CompactDimension.LEVEL_KEY);
            final var graph = TunnelConnectionGraph.forRoom(compactDim, room);
            final var attachedTunnelsStream = graph.getTypesForSide(machine.getLevelPosition(), accessor.getSide());

            ItemStack[] itemStackArray = attachedTunnelsStream
                    .map(TunnelItem::createStack)
                    .toArray(ItemStack[]::new);

            ListTag attachedTunnels = new ListTag();
            for (ItemStack itemStack : itemStackArray) {
                CompoundTag tag = itemStack.save(new CompoundTag());
                attachedTunnels.add(tag);
            }

            data.put("attached_tunnels", attachedTunnels);
        });
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Constants.MOD_ID, "machine_tunnels");
    }
}
