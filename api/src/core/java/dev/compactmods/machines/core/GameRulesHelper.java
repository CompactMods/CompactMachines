package dev.compactmods.machines.core;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public abstract class GameRulesHelper {

     public static GameRule<Boolean> makeBooleanRule(boolean defaultVal) {
        return new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(),
                GameRuleTypeVisitor::visitBoolean, Codec.BOOL, (b) -> b ? 1 : 0, defaultVal, FeatureFlags.DEFAULT_FLAGS);
    }
}
