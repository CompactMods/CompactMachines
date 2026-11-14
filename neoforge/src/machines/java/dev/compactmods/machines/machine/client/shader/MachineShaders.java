package dev.compactmods.machines.machine.client.shader;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface MachineShaders {

    RenderPipeline.Snippet SEMITRANSPARENT_SNIPPET = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withCull(true)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .buildSnippet();

    /** Tye-dye lava-lamp pipeline. */
    RenderPipeline TYE_DYE_PIPELINE = RenderPipeline.builder(SEMITRANSPARENT_SNIPPET)
            .withLocation(CompactMachinesCore.identifier("pipeline/tye_dye"))
            .withVertexShader(CompactMachinesCore.identifier("tye_dye"))
            .withFragmentShader(CompactMachinesCore.identifier("tye_dye"))
            .build();

    /**
     * Render type that draws geometry through {@link #TYE_DYE_PIPELINE}.
     *
     * <p>Mirrors vanilla {@code TRANSLUCENT_MOVING_BLOCK}: vertices get sorted
     * back-to-front on upload, and the geometry is rendered to
     * {@link OutputTarget#ITEM_ENTITY_TARGET} — the translucent-compositing
     * framebuffer — so the overlay composes correctly with the chunk
     * translucent pass (water, glass, other compact machines) instead of being
     * depth-occluded by it.</p>
     */
    RenderType TYE_DYE_RENDER_TYPE = RenderType.create(
            "compactmachines:tye_dye",
            RenderSetup.builder(TYE_DYE_PIPELINE)
//                    .sortOnUpload()
//                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup());
}
