package dev.compactmods.machines.datagen.models;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;

public class CompactMachineModelTemplate {

    public static final TextureSlot OVERLAY_SLOT = TextureSlot.create("overlay");
    public static final TextureSlot TINT_SLOT = TextureSlot.create("tint");
    public static final TextureSlot BORDER_SLOT = TextureSlot.create("border");

    public static final ModelTemplate MACHINE_TEMPLATE = ModelTemplates.create(CompactMachines.id("machine"),
                    TextureSlot.PARTICLE,
                    BORDER_SLOT,
                    TINT_SLOT,
                    OVERLAY_SLOT)
            .extend()
            .parent(ResourceLocation.withDefaultNamespace("block/block"))
            .renderType("cutout")
            .element(border -> border.allFaces((dir, face) -> face.texture(BORDER_SLOT)
                    .uvs(0, 0, 16, 16)
                    .cullface(dir)))
            .element(tint -> tint.allFaces((dir, face) -> face.texture(TINT_SLOT)
                    .emissivity(2, 0)
                    .uvs(0, 0, 16, 16)
                    .cullface(dir)
                    .tintindex(0)))
            .element(overlay -> overlay.allFaces((dir, face) -> face.texture(OVERLAY_SLOT)
                    .uvs(0, 0, 16, 16)
                    .cullface(dir)
                    .tintindex(1)))
            .build();

}
