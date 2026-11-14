package dev.compactmods.machines.core.mixin;

import dev.compactmods.machines.core.attachment.IForwardingAttachmentHolder;
import dev.compactmods.machines.core.capability.IServerCapabilityHolder;
import dev.compactmods.machines.core.capability.ServerCapability;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IForwardingAttachmentHolder, IServerCapabilityHolder {

    @Unique
    private final AttachmentHolder cm_attachments = new AttachmentHolder.AsField(this);

    @Override
    public Supplier<IAttachmentHolder> getAttachmentHolder() {
        return () -> cm_attachments;
    }

    // ─── ServerCapability lookup ────────────────────────────────────────────
    //
    // These methods satisfy IServerCapabilityHolder (declared on the class) and
    // are merged into MinecraftServer by Mixin. The matching interface listing
    // in core/interfaces.json makes the methods visible to javac on every
    // consumer that depends on :core, so callers can write
    // `server.getCapability(...)` directly.
    //
    // The (MinecraftServer)(Object) cast is required because
    // IServerCapabilityHolder does NOT declare `extends MinecraftServer` —
    // that's the whole point of keeping it loosely coupled — and javac
    // otherwise refuses an unrelated-types cast. The intermediate Object cast
    // suppresses the check; the JVM does the real type check at runtime, and
    // because the mixin only targets MinecraftServer the cast can never fail.

    @Override
    public <T> @Nullable T getCapability(ServerCapability<T, Void> capability) {
        return capability.getCapability((MinecraftServer) (Object) this);
    }

    @Override
    public <T, C> @Nullable T getCapability(ServerCapability<T, C> capability, @Nullable C context) {
        return capability.getCapability((MinecraftServer) (Object) this, context);
    }
}
