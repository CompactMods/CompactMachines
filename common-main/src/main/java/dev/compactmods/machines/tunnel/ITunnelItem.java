package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface ITunnelItem {
    static Optional<TunnelDefinition> getDefinition(ItemStack stack) {
        CompoundTag defTag = stack.getOrCreateTagElement("definition");
        if (defTag.isEmpty() || !defTag.contains("id"))
            return Optional.empty();

        ResourceLocation defId = new ResourceLocation(defTag.getString("id"));
        var defRegistry = TunnelHelper.definitionRegistry();
        if (!defRegistry.containsKey(defId))
            return Optional.empty();

        TunnelDefinition tunnelReg = defRegistry.get(defId);
        return Optional.ofNullable(tunnelReg);
    }
}
