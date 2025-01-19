package dev.compactmods.machines.gamerule;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.world.level.GameRules;

public class CMGameRules {

    public static GameRules.Key<GameRules.BooleanValue> ALLOW_SURVIVAL_OUT_OF_BOUNDS;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_CREATIVE_OUT_OF_BOUNDS;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_SPECTATORS_OUT_OF_BOUNDS;
    public static GameRules.Key<GameRules.BooleanValue> DAMAGE_OOB_PLAYERS;

    /**
     * For hardcore-style packs. If a shrinking item is successfully used to LEAVE a room,
     * it will also be damaged. Off by default.
     */
    public static GameRules.Key<GameRules.BooleanValue> DAMAGE_PSD_ITEMS_ON_ROOM_EXIT;

    public static void register() {
        ALLOW_SURVIVAL_OUT_OF_BOUNDS = GameRules.register(CompactMachines.dotPrefix("allow_survival_oob"), GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        ALLOW_CREATIVE_OUT_OF_BOUNDS = GameRules.register(CompactMachines.dotPrefix("allow_creative_oob"), GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        ALLOW_SPECTATORS_OUT_OF_BOUNDS = GameRules.register(CompactMachines.dotPrefix("allow_spectator_oob"), GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        DAMAGE_OOB_PLAYERS = GameRules.register(CompactMachines.dotPrefix("damage_oob"), GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        DAMAGE_PSD_ITEMS_ON_ROOM_EXIT = GameRules.register(CompactMachines.dotPrefix("damage_psd_on_exit"), GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    }
}
