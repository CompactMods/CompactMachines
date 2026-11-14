package dev.compactmods.machines.dimension;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;


public class VoidAirBlock extends AirBlock {
    // FIXME final public static DamageSource DAMAGE_SOURCE = new DamageSource(MOD_ID + "_voidair");

    public static final ResourceKey<Block> RESOURCE_KEY = ResourceKey.create(Registries.BLOCK, CompactMachinesCore.identifier("void_air"));

    public VoidAirBlock() {
        super(BlockBehaviour.Properties.of()
                .setId(RESOURCE_KEY)
                .isValidSpawn((state, level, pos, entity) -> false)
                .strength(-1.0F, 3600000.0F)
                .noTerrainParticles()
                .noLootTable()
                .forceSolidOn());
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean p_451772_) {
        if (level.isClientSide()) return;
        if (!CompactDimension.isLevelCompact(level)) return;

        if (entity instanceof ServerPlayer player) {
            // If players are allowed outside of machine bounds, early exit -- but damage them if configured
            final var rules = player.level().getGameRules();

            if (rules.get(CMGameRules.DAMAGE_OOB_PLAYERS.get()))
                tryDamagingAdventurousPlayer(level, player);

            // FIXME - Achievement
            // PlayerUtil.howDidYouGetThere(player);

            boolean allowedOutOfBounds = switch (player.gameMode.getGameModeForPlayer()) {
                case GameType.ADVENTURE, GameType.SURVIVAL ->
                        rules.get(CMGameRules.ALLOW_SURVIVAL_OUT_OF_BOUNDS.get());
                case GameType.CREATIVE -> rules.get(CMGameRules.ALLOW_CREATIVE_OUT_OF_BOUNDS.get());
                case GameType.SPECTATOR -> rules.get(CMGameRules.ALLOW_SPECTATORS_OUT_OF_BOUNDS.get());
            };

            if (!allowedOutOfBounds)
                ShrinkingHelper.teleportPlayerToRespawnOrOverworld(level.getServer(), player);
        }
    }

    /**
     * Attempts to inflict the bad effects on players that are touching a void air block.
     * Does nothing to players that are in creative or spectator mode.
     *
     * @param pLevel
     * @param player
     */
    private static void tryDamagingAdventurousPlayer(Level pLevel, ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        if (!player.hasEffect(MobEffects.NAUSEA)) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 5 * 20));
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 5 * 20));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20));
        }

        // Damage the player down to half a heart
        if (player.getHealth() > 1) {
            player.hurt(pLevel.damageSources().fellOutOfWorld(), player.getHealth() - 1);
        }
    }
}
