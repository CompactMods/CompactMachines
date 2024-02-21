package dev.compactmods.machines.neoforge.compat.theoneprobe;

import dev.compactmods.machines.neoforge.compat.theoneprobe.elements.PlayerFaceElement;
import dev.compactmods.machines.neoforge.compat.theoneprobe.overrides.CompactMachineNameOverride;
import dev.compactmods.machines.neoforge.compat.theoneprobe.providers.CompactMachineProvider;
import dev.compactmods.machines.neoforge.compat.theoneprobe.providers.TunnelProvider;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class TheOneProbeMain implements Function<Object, Void> {
    static ITheOneProbe PROBE;

    @Override
    public Void apply(Object o) {
        PROBE = (ITheOneProbe) o;
        PROBE.registerBlockDisplayOverride(new CompactMachineNameOverride());
        PROBE.registerProvider(new CompactMachineProvider());
        PROBE.registerProvider(new TunnelProvider());

        PROBE.registerElementFactory(new IElementFactory() {
            @Override
            public IElement createElement(FriendlyByteBuf buffer) {
                return new PlayerFaceElement(buffer.readGameProfile());
            }

            @Override
            public ResourceLocation getId() {
                return PlayerFaceElement.ID;
            }
        });

        return null;
    }

}
