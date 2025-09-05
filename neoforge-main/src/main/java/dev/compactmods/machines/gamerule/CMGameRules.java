package dev.compactmods.machines.gamerule;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.world.level.GameRules;

public class CMGameRules {

    public static final String ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY = CompactMachines.dotPrefix("allow_survival_oob");
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_SURVIVAL_OUT_OF_BOUNDS;

    public static final String ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY = CompactMachines.dotPrefix("allow_creative_oob");
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_CREATIVE_OUT_OF_BOUNDS;

    public static GameRules.Key<GameRules.BooleanValue> ALLOW_SPECTATORS_OUT_OF_BOUNDS;
    public static final String ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY = CompactMachines.dotPrefix("allow_spectator_oob");

    public static final String DAMAGE_OOB_PLAYERS_KEY = CompactMachines.dotPrefix("damage_oob");
    public static GameRules.Key<GameRules.BooleanValue> DAMAGE_OOB_PLAYERS;

    /**
     * For hardcore-style packs. If a shrinking item is successfully used to LEAVE a room,
     * it will also be damaged. Off by default.
     */
    public static final String DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY = CompactMachines.dotPrefix("damage_psd_on_exit");
    public static GameRules.Key<GameRules.BooleanValue> DAMAGE_PSD_ITEMS_ON_ROOM_EXIT;

    public static void register() {
        ALLOW_SURVIVAL_OUT_OF_BOUNDS = GameRules.register(ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY, GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        ALLOW_CREATIVE_OUT_OF_BOUNDS = GameRules.register(ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY, GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        ALLOW_SPECTATORS_OUT_OF_BOUNDS = GameRules.register(ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY, GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        DAMAGE_OOB_PLAYERS = GameRules.register(DAMAGE_OOB_PLAYERS_KEY, GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        DAMAGE_PSD_ITEMS_ON_ROOM_EXIT = GameRules.register(DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY, GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    }
}
