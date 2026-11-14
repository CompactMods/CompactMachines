package dev.compactmods.machines.gamerule;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.GameRulesHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CMGameRules {

    private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(BuiltInRegistries.GAME_RULE, CompactMachinesCore.MOD_ID);

    public static final Identifier ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY = CompactMachinesCore.identifier("allow_survival_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SURVIVAL_OUT_OF_BOUNDS = GAME_RULES
            .register(ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

    public static final Identifier ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY = CompactMachinesCore.identifier("allow_creative_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_CREATIVE_OUT_OF_BOUNDS = GAME_RULES
            .register(ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(true));

    public static final Identifier ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY = CompactMachinesCore.identifier("allow_spectator_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_SPECTATORS_OUT_OF_BOUNDS = GAME_RULES
            .register(ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

    public static final Identifier DAMAGE_OOB_PLAYERS_KEY = CompactMachinesCore.identifier("damage_oob");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> DAMAGE_OOB_PLAYERS = GAME_RULES
            .register(DAMAGE_OOB_PLAYERS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

    public static final Identifier ALLOW_BIG_ROOMS_KEY = CompactMachinesCore.identifier("i_do_not_care_about_performance");
    public static DeferredHolder<GameRule<?>, GameRule<Boolean>> ALLOW_BIG_ROOMS = GAME_RULES
            .register(ALLOW_BIG_ROOMS_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

    public static void init(IEventBus modBus) {
        GAME_RULES.register(modBus);
    }
}
