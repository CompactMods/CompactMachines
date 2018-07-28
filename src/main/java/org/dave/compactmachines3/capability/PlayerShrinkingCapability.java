package org.dave.compactmachines3.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.dave.compactmachines3.api.IPlayerShrinkingCapability;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class PlayerShrinkingCapability {
    @CapabilityInject(IPlayerShrinkingCapability.class)
    public static Capability<IPlayerShrinkingCapability> PLAYER_SHRINKING_CAPABILITY = null;

    public static void init() {
        CapabilityManager.INSTANCE.register(IPlayerShrinkingCapability.class, new Storage(), new Factory());
    }

    public static boolean isShrinkingDevice(ItemStack stack) {
        if(stack.isEmpty() || stack == null) {
            return false;
        }

        return stack.hasCapability(PlayerShrinkingCapability.PLAYER_SHRINKING_CAPABILITY, null);
    }

    public static boolean hasShrinkingDeviceInHand(EntityPlayer player) {
        return isShrinkingDevice(player.getHeldItemMainhand()) || isShrinkingDevice(player.getHeldItemOffhand());
    }

    public static boolean hasShrinkingDeviceInHand(EntityPlayer player, EnumHand hand) {
        return isShrinkingDevice(player.getHeldItem(hand));
    }

    public static boolean hasShrinkingDeviceInInventory(EntityPlayer player) {
        for(int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if(isShrinkingDevice(stack)) {
                return true;
            }
        }

        return false;
    }

    private static class Storage implements Capability.IStorage<IPlayerShrinkingCapability> {

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IPlayerShrinkingCapability> capability, IPlayerShrinkingCapability instance, EnumFacing side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IPlayerShrinkingCapability> capability, IPlayerShrinkingCapability instance, EnumFacing side, NBTBase nbt) {
        }
    }

    private  static class Factory implements Callable<IPlayerShrinkingCapability> {

        @Override
        public IPlayerShrinkingCapability call() throws Exception {
            return new IPlayerShrinkingCapability() {};
        }
    }
}
