package dev.compactmods.machines.neoforge;

import com.mojang.datafixers.kinds.Const;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.neoforge.client.ClientConfig;
import dev.compactmods.machines.neoforge.client.creative.CreativeTabs;
import dev.compactmods.machines.neoforge.config.CommonConfig;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.data.functions.LootFunctions;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import dev.compactmods.machines.neoforge.villager.Villagers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class CompactMachines {

    public static final int BRAND_MACHINE_COLOR = FastColor.ARGB32.color(255, 248, 246, 76);

    @SuppressWarnings("unused")
    public CompactMachines(IEventBus modBus) {
        // Package initialization here, this kick-starts the rest of the DR code (classloading)
        Machines.prepare();
        Shrinking.prepare();
        Rooms.prepare();
        Dimension.prepare();
//  todo upgrade system      MachineRoomUpgrades.prepare();
        Commands.prepare();
        LootFunctions.prepare();

        Villagers.prepare();
        CreativeTabs.prepare();

        Registries.setup(modBus);

        // Configuration
        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);
    }

    public static ResourceLocation rl(String id) {
        return new ResourceLocation(Constants.MOD_ID, id);
    }
}
