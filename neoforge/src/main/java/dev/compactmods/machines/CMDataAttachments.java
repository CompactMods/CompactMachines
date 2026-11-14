package dev.compactmods.machines;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public interface CMDataAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachinesCore.MOD_ID);

    /**
     * Selects which renderer drives a Compact Machine's glass faces.
     */
    Supplier<AttachmentType<Identifier>> MACHINE_SHADER = ATTACHMENT_TYPES.register("machine_shader",
            () -> AttachmentType.builder(() -> {
                        return CompactMachinesCore.identifier("none");
                    })
                    .serialize(Identifier.CODEC.fieldOf("shader_id"), id -> !(id.getNamespace().equals(CompactMachinesCore.MOD_ID) && id.getPath().equals("none")))
                    .build());

    static void prepare() {
    }

}
