package dev.compactmods.machines.neoforge.dimension;

import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.util.ForgePlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;


public class VoidAirBlock extends AirBlock {
    // FIXME final public static DamageSource DAMAGE_SOURCE = new DamageSource(MOD_ID + "_voidair");

    public VoidAirBlock() {
        super(BlockBehaviour.Properties.of()
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
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (ServerConfig.isAllowedOutsideOfMachine()) return;
        if (pLevel.isClientSide) return;

        // TODO: Configurable behavior
        if (pEntity instanceof ServerPlayer player) {
            if (player.isCreative()) return;

            player.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * 20));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20));
            player.hurt(pLevel.damageSources().fellOutOfWorld(), 1);

            // FIXME - Achievement
            // PlayerUtil.howDidYouGetThere(player);
            // player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(IRoomHistory::clear);
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(player.server, player);
        }
    }
}
