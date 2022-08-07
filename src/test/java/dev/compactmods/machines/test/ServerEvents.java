package dev.compactmods.machines.test;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.util.DimensionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent evt) {
        final MinecraftServer serv = evt.getServer();
        var compactLevel = serv.getLevel(CompactDimension.LEVEL_KEY);
        if (compactLevel == null) {
            CompactMachines.LOGGER.warn("Compact dimension not found; recreating it.");
            DimensionUtil.createAndRegisterWorldAndDimension(serv);
        }
    }
}
