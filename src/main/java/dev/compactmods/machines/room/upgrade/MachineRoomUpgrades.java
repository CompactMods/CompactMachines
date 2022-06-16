package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class MachineRoomUpgrades {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(CompactMachines.MOD_ID, "room_upgrades");
    public static final ResourceKey<Registry<RoomUpgrade>> REGISTRY_KEY = ResourceKey.createRegistryKey(REGISTRY_NAME);

    private static final DeferredRegister<RoomUpgrade> UPGRADES = DeferredRegister.create(REGISTRY_KEY, CompactMachines.MOD_ID);
    public static final Supplier<IForgeRegistry<RoomUpgrade>> REGISTRY = UPGRADES.makeRegistry(RoomUpgrade.class, RegistryBuilder::new);

    // ================================================================================================================
    static final RegistryObject<RoomUpgrade> CHUNKLOAD = UPGRADES.register(ChunkloadUpgrade.REG_ID.getPath(), ChunkloadUpgrade::new);


    // ================================================================================================================

    public static void init(IEventBus bus) {
        UPGRADES.register(bus);
    }
}
