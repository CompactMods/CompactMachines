package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class FakeTunnelDefinition implements TunnelDefinition {

    public static final ResourceKey<TunnelDefinition> ID = ResourceKey.create(TunnelDefinition.REGISTRY_KEY, new ResourceLocation(Constants.MOD_ID, "fake"));

    @Override
    public int ringColor() {
        return FastColor.ARGB32.color(255, 255, 0, 0);
    }
}
