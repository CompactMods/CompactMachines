package dev.compactmods.machines.core.mixin;

import dev.compactmods.machines.core.sync.BlockEntitySyncHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reports block entity changes ({@code BlockEntity#setChanged}) to {@link BlockEntitySyncHooks}. */
@Mixin(Level.class)
public abstract class LevelBlockEntityChangedMixin {

    @Inject(method = "blockEntityChanged", at = @At("HEAD"))
    private void cm_onBlockEntityChanged(BlockPos pos, CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel level)
            BlockEntitySyncHooks.blockChanged(level, pos);
    }
}
