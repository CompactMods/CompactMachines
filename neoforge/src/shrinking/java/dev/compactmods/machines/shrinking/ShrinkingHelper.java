package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.api.dimension.CompactDimensionTransitions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShrinkingHelper {
    public static void handleSuccessfulAtomicShift(ServerPlayer serverPlayer, ItemStack stack) {
        final var config = stack.get(Shrinking.DataComponents.SHRINKING_CONFIG);
        if(config == null)
            return;
        
        switch (config.afterUseAction()) {
            case TRY_DAMAGE_ITEM:
                if (!serverPlayer.hasInfiniteMaterials()) {
                    stack.hurtAndBreak(1, serverPlayer.level(), serverPlayer, item -> {
                        // RIP, hope you have spare crafting materials nearby!
                    });
                }
                break;

            case DESTROY_ITEM:
                if (!serverPlayer.hasInfiniteMaterials()) {
                    stack.consume(1, serverPlayer);

                    if (!serverPlayer.isSilent()) {

                        var l = serverPlayer.level();
                        var pos = serverPlayer.position();

                        l.playSeededSound(
                                null,
                                pos.x(), pos.y(), pos.z(),
                                stack.getOrDefault(DataComponents.BREAK_SOUND, SoundEvents.ITEM_BREAK),
                                serverPlayer.getSoundSource(),
                                1.0F,
                                0.8F + l.getRandom().nextFloat() * 0.4F,
                                serverPlayer.getRandom().nextLong()
                        );
                    }
                }
                break;
        }
    }

    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @NotNull ServerPlayer player) {
        final var config = player.getRespawnConfig();

        player.removeData(Shrinking.CURRENT_ROOM_CODE);

        final var historyManager = player.getCapability(Shrinking.HISTORY_MANAGER);
        if(historyManager != null)
            historyManager.clear();

        final var transition = Optional.ofNullable(config)
                .map(c -> CompactDimensionTransitions.to(serv.getLevel(c.respawnData().dimension()), Vec3.atBottomCenterOf(c.respawnData().pos())))
                .orElse(CompactDimensionTransitions.to(serv.overworld(),
                        Vec3.atBottomCenterOf(serv.overworld().getRespawnData().pos())));

        player.teleport(transition);
    }
}
