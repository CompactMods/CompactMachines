package dev.compactmods.machines;

import dev.compactmods.machines.api.CompactMachinesAddon;
import dev.compactmods.machines.api.ICompactMachinesAddon;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.config.CommonConfig;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.room.ui.RoomUserInterfaceRegistration;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.LootFunctions;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.villager.Villagers;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod(Constants.MOD_ID)
public class CompactMachines implements ICompactMachinesMod {
    /**
     * @deprecated Switch usages to use api Constants in 1.20, eliminate it here
     */
    @Deprecated(forRemoval = true)
    public static final String MOD_ID = Constants.MOD_ID;

    public static final Marker ADDON_LIFECYCLE = MarkerManager.getMarker("addons");

    public static final CreativeModeTab COMPACT_MACHINES_ITEMS = new CreativeModeTab(Constants.MOD_ID) {
        @Override
        public @Nonnull
        ItemStack makeIcon() {
            return new ItemStack(Machines.MACHINE_BLOCK_ITEM_NORMAL.get());
        }
    };

    private static Set<ICompactMachinesAddon> loadedAddons;

    public CompactMachines() {
        Registries.setup();
        preparePackages();
        doRegistration();

        // Configuration
        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent caps) {
        caps.register(IPlayerRoomMetadataProvider.class);
        caps.register(IRoomHistory.class);
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
        Registries.VILLAGERS.register(bus);
        Villagers.TRADES.register(bus);
        Registries.POINTS_OF_INTEREST.register(bus);

        CompactMachines.loadedAddons = ModList.get()
                .getAllScanData()
                .stream()
                .flatMap(scans -> scans.getAnnotations()
                        .stream()
                        .filter(ad -> ad.annotationType().equals(Type.getType(CompactMachinesAddon.class)))
                        .map(ModFileScanData.AnnotationData::memberName)
                        .map(cmAddonClass -> {
                            try {
                                final var cl = Class.forName(cmAddonClass);
                                final var cla = cl.asSubclass(ICompactMachinesAddon.class);
                                return cla.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull))
                .collect(Collectors.toSet());

        CompactMachines.loadedAddons.forEach(addon -> {
            LOGGER.debug(ADDON_LIFECYCLE, "Sending registration hook to addon: {}", addon.getClass().getName());
            addon.afterRegistration();
        });
    }

    private static void preparePackages() {
        // Package initialization here, this kickstarts the rest of the DR code (classloading)
        Machines.prepare();
        Walls.prepare();
        Tunnels.prepare();
        Shrinking.prepare();

        RoomUserInterfaceRegistration.prepare();
        Dimension.prepare();
        Rooms.prepare();
        MachineRoomUpgrades.prepare();
        GraphCommon.prepare();
        Commands.prepare();
        LootFunctions.prepare();

        Villagers.prepare();
    }

    public static Set<ICompactMachinesAddon> getAddons() {
        return loadedAddons;
    }
}
