package dev.compactmods.machines.forgebuiltin.upgrade;

import dev.compactmods.machines.api.core.CMRegistryKeys;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BuiltInUpgrades {

    public static DeferredRegister<RoomUpgrade> REGISTRATION =
            DeferredRegister.create(CMRegistryKeys.ROOM_UPGRADES, Constants.MOD_ID);

    public static final RegistryObject<RoomUpgrade> CHUNKLOAD =
            REGISTRATION.register("chunkload", ChunkloadAction::new);

    public static void prepare() {}
}
