package dev.compactmods.machines.core.mixin;

import dev.compactmods.machines.core.sync.BlockEntitySyncHooks;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reports mod packets sent to the players tracking a chunk to {@link BlockEntitySyncHooks}. Mods use
 * this for their own block entity updates (Mekanism's transporter items), which vanilla never sees.
 */
@Mixin(value = PacketDistributor.class, remap = false)
public abstract class PacketDistributorMixin {

    @Inject(method = "sendToPlayersTrackingChunk", at = @At("HEAD"))
    private static void cm_onSendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload,
                                                        CustomPacketPayload[] payloads, CallbackInfo ci) {
        BlockEntitySyncHooks.chunkSynced(level, chunkPos);
    }
}
