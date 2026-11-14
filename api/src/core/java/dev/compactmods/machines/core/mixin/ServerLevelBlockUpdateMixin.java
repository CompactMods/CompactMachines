package dev.compactmods.machines.core.mixin;

import dev.compactmods.machines.core.sync.BlockEntitySyncHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reports block updates sent to clients (vanilla's block entity sync) to {@link BlockEntitySyncHooks}. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelBlockUpdateMixin {

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void cm_onSendBlockUpdated(BlockPos pos, BlockState old, BlockState current, int updateFlags, CallbackInfo ci) {
        BlockEntitySyncHooks.blockChanged((ServerLevel) (Object) this, pos);
    }
}
