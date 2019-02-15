package org.dave.compactmachines3.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.integration.AbstractNullHandler;
import org.dave.compactmachines3.integration.CapabilityNullHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@CapabilityNullHandler(mod = "refinedstorage")
public class NetworkNodeNullHandler extends AbstractNullHandler implements INetworkNodeProxy {
    @CapabilityInject(INetworkNodeProxy.class)
    public static Capability<INetworkNodeProxy> NETWORK_NODE_PROXY_CAPABILITY = null;

    @Nonnull
    @Override
    public INetworkNode getNode() {
        return new DummyNetworkNode();
    }

    @Override
    public Capability getCapability() {
        return NETWORK_NODE_PROXY_CAPABILITY;
    }

    public static class DummyNetworkNode implements INetworkNode {

        @Override
        public int getEnergyUsage() {
            return 0;
        }

        @Nonnull
        @Override
        public ItemStack getItemStack() {
            return new ItemStack(Blockss.tunnel);
        }

        @Override
        public void onConnected(INetwork network) {
        }

        @Override
        public void onDisconnected(INetwork network) {
        }

        @Override
        public boolean canUpdate() {
            return false;
        }

        @Nullable
        @Override
        public INetwork getNetwork() {
            return null;
        }

        @Override
        public void update() {
        }

        @Override
        public NBTTagCompound write(NBTTagCompound tag) {
            return null;
        }

        @Override
        public BlockPos getPos() {
            return new BlockPos(0,0,0);
        }

        @Override
        public World getWorld() {
            return null;
        }

        @Override
        public void markDirty() {
        }

        @Override
        public String getId() {
            return "";
        }
    }
}