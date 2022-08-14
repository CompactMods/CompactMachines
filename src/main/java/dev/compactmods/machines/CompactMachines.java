package dev.compactmods.machines;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.config.CommonConfig;
import dev.compactmods.machines.config.EnableVanillaRecipesConfigCondition;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.core.UIRegistration;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.graph.Graph;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.LootFunctions;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;

@Mod(Constants.MOD_ID)
public class CompactMachines {
    /**
     * @deprecated Switch usages to use api Constants in 1.20, eliminate it here
     */
    @Deprecated(forRemoval = true)
    public static final String MOD_ID = Constants.MOD_ID;

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Marker CONN_MARKER = MarkerManager.getMarker("cm_connections");

    public static final CreativeModeTab COMPACT_MACHINES_ITEMS = new CreativeModeTab(Constants.MOD_ID) {
        @Override
        public @Nonnull
        ItemStack makeIcon() {
            return new ItemStack(Machines.MACHINE_BLOCK_ITEM_NORMAL.get());
        }
    };

    public CompactMachines() {
        Registries.setup();
        preparePackages();
        doRegistration();

        // Configuration
        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CraftingHelper.register(EnableVanillaRecipesConfigCondition.Serializer.INSTANCE);
    }

    /**
     * Sets up the deferred registration for usage in package/module setup.
     */
    private static void doRegistration() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        Registries.BLOCKS.register(bus);
        Registries.ITEMS.register(bus);
        Registries.BLOCK_ENTITIES.register(bus);
        Registries.TUNNEL_DEFINITIONS.register(bus);
        Registries.CONTAINERS.register(bus);
        Registries.ROOM_TEMPLATES.register(bus);
        Registries.UPGRADES.register(bus);
        Registries.NODE_TYPES.register(bus);
        Registries.EDGE_TYPES.register(bus);
        Registries.COMMAND_ARGUMENT_TYPES.register(bus);
        Registries.LOOT_FUNCS.register(bus);
    }

    private static void preparePackages() {
        // Package initialization here, this kickstarts the rest of the DR code (classloading)
        Machines.prepare();
        Walls.prepare();
        Tunnels.prepare();
        Shrinking.prepare();

        UIRegistration.prepare();
        Dimension.prepare();
        Rooms.prepare();
        MachineRoomUpgrades.prepare();
        Graph.prepare();
        Commands.prepare();
        LootFunctions.prepare();
    }
}
