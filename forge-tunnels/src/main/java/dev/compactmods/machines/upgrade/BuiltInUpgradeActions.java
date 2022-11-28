package dev.compactmods.machines.upgrade;

import dev.compactmods.machines.api.core.CMRegistryKeys;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.upgrade.RoomUpgradeAction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BuiltInUpgradeActions {

    public static DeferredRegister<RoomUpgradeAction> REGISTRATION =
            DeferredRegister.create(CMRegistryKeys.UPGRADE_ACTIONS, Constants.MOD_ID);

    public static final RegistryObject<RoomUpgradeAction> CHUNKLOAD =
            REGISTRATION.register("chunkload", ChunkloadAction::new);

    public static void prepare() {}
}
