package dev.compactmods.machines.test;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.test.tunnel.FakeTunnelDefinition;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TestRegistration {

    @SubscribeEvent
    public static void onRegisterEvent(final RegisterEvent e) {
        if (e.getRegistryKey().equals(TunnelDefinition.REGISTRY_KEY)) {
            e.register(TunnelDefinition.REGISTRY_KEY, (helper) -> {
                final var inst = new FakeTunnelDefinition();
                helper.register(FakeTunnelDefinition.ID, inst);
            });
        }
    }
}
