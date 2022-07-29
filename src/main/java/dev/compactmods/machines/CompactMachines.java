package dev.compactmods.machines;

import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.config.CommonConfig;
import dev.compactmods.machines.config.EnableVanillaRecipesConfigCondition;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.core.UIRegistration;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.upgrade.command.RoomUpgradeArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
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

@Mod(CompactMachines.MOD_ID)
public class CompactMachines {
    public static final String MOD_ID = "compactmachines";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Marker CONN_MARKER = MarkerManager.getMarker("cm_connections");

    public static final CreativeModeTab COMPACT_MACHINES_ITEMS = new CreativeModeTab(MOD_ID) {
        @Override
        public @Nonnull
        ItemStack makeIcon() {
            return new ItemStack(Registration.MACHINE_BLOCK_ITEM_NORMAL.get());
        }
    };

    // public static CMRoomChunkloadingManager CHUNKLOAD_MANAGER;

    public CompactMachines() {
        // Register blocks and items
        var eb = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.init(eb);
        UIRegistration.init(eb);
        Tunnels.init(eb);
        CMGraphRegistration.init(eb);
        MachineRoomUpgrades.init(eb);

        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CraftingHelper.register(EnableVanillaRecipesConfigCondition.Serializer.INSTANCE);

        ArgumentTypes.register("room_pos", RoomPositionArgument.class, new EmptyArgumentSerializer<>(RoomPositionArgument::room));
        ArgumentTypes.register("upgrade_type", RoomUpgradeArgument.class, new EmptyArgumentSerializer<>(RoomUpgradeArgument::upgrade));
    }
}
