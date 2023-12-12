package dev.compactmods.machines.forge;

import dev.compactmods.machines.ICompactMachinesMod;
import dev.compactmods.machines.api.CompactMachinesAddon;
import dev.compactmods.machines.api.ICompactMachinesAddon;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.forge.client.ClientConfig;
import dev.compactmods.machines.forge.config.CommonConfig;
import dev.compactmods.machines.forge.config.ServerConfig;
import dev.compactmods.machines.forge.data.functions.LootFunctions;
import dev.compactmods.machines.forge.dimension.Dimension;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.room.Rooms;
import dev.compactmods.machines.forge.room.ui.RoomUserInterfaceRegistration;
import dev.compactmods.machines.forge.shrinking.Shrinking;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.forge.util.AnnotationScanner;
import dev.compactmods.machines.forge.villager.Villagers;
import dev.compactmods.machines.forge.wall.Walls;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod(Constants.MOD_ID)
public class CompactMachines implements ICompactMachinesMod {
    /**
     * @deprecated Switch usages to use api {@link Constants#MOD_ID} in 1.20, eliminate it here
     */
    @Deprecated(forRemoval = true, since = "5.2.0")
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
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
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
        Registries.COMMAND_ARGUMENT_TYPES.register(bus);
        Registries.LOOT_FUNCS.register(bus);
        Registries.VILLAGERS.register(bus);
        // Villagers.TRADES.register(bus);
        Registries.POINTS_OF_INTEREST.register(bus);

//        CompactMachines.loadedAddons = ServiceLoader.load(ICompactMachinesAddon.class)
//                .stream()
//                .filter(p -> Arrays.stream(p.type().getAnnotationsByType(CompactMachinesAddon.class))
//                        .anyMatch(cma -> cma.major() == 2))
//                .map(allowedThisMajor -> {
//                    allowedThisMajor.get();
//                })
//                .collect(Collectors.toSet());

        CompactMachines.loadedAddons = AnnotationScanner.scanModList(CompactMachinesAddon.class)
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
                .filter(Objects::nonNull)
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
        Commands.prepare();
        LootFunctions.prepare();

        Villagers.prepare();
    }

    public static Set<ICompactMachinesAddon> getAddons() {
        return loadedAddons;
    }
}
