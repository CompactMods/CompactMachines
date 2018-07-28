package org.dave.compactmachines3.misc;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.capability.PlayerShrinkingCapability;
import org.dave.compactmachines3.init.Itemss;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityEventHandler {

    private static boolean isIntegratedShrinkingDevice(Item item) {
        return item == Itemss.psd;
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesToItems(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if(isIntegratedShrinkingDevice(stack.getItem()) && !PlayerShrinkingCapability.isShrinkingDevice(stack)) {
            event.addCapability(new ResourceLocation(CompactMachines3.MODID, "canShrinkPlayers"), new PlayerShrinkingCapabilityProvider());
        }
    }

    private static class PlayerShrinkingCapabilityProvider implements ICapabilityProvider {

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == PlayerShrinkingCapability.PLAYER_SHRINKING_CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return (T) PlayerShrinkingCapability.PLAYER_SHRINKING_CAPABILITY.getDefaultInstance();
        }
    }
}
