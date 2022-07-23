package dev.compactmods.machines.wall;

import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class MachineVoidAir extends AirBlock {
    final public static DamageSource DAMAGE_SOURCE = new DamageSource("machinevoidair");

    public MachineVoidAir() {
        super(BlockBehaviour.Properties.of(Material.AIR).noCollission().noDrops().air());
    }


    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (pLevel.isClientSide) return;

        if (pEntity instanceof ServerPlayer player) {
            if (player.isCreative()) return;

            if (player.getActiveEffectsMap().containsKey(MobEffects.BLINDNESS))
                PlayerUtil.teleportPlayerBackToMachine((ServerLevel) pLevel, player);


            player.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * 20));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20));
            player.hurt(DAMAGE_SOURCE, 1);
        }
    }
}
