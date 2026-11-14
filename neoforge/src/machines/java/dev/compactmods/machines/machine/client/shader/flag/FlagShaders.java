package dev.compactmods.machines.machine.client.shader.flag;

import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class FlagShaders {

    // Happy Pride y'all
    public static final RenderPipeline PRIDE_STRIPES_PIPELINE;
    public static final BindGroupLayout FLAG_PALETTE;

    static {
        // Palette UBO — populated each frame by MachineShaderRenderer from
        // the resolved FlagShader registry entry. See pride_stripes.fsh for
        // the std140 layout of the block.
        FLAG_PALETTE = BindGroupLayout.builder()
                .withUniform("FlagPalette", UniformType.UNIFORM_BUFFER)
                .build();

        PRIDE_STRIPES_PIPELINE = RenderPipeline.builder()
                .withLocation(CompactMachinesCore.identifier("pride_stripes"))
                .withVertexShader(CompactMachinesCore.identifier("pride_stripes"))
                .withFragmentShader(CompactMachinesCore.identifier("pride_stripes"))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                // Exactly the bind groups pride_stripes.{vsh,fsh} reference:
                //   GLOBALS            — CameraBlockPos / CameraOffset (globals.glsl)
                //   DYNAMIC_TRANSFORMS — DynamicTransforms (ModelViewMat)
                //   PROJECTION         — Projection (ProjMat)
                //   FLAG_PALETTE       — the custom stripe-colour UBO
                // Don't inherit BLOCK_SNIPPET here: it also pulls in Fog +
                // Sampler0/Sampler2 + the core/block shader/vertex-format defaults,
                // none of which this shader uses.
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(FLAG_PALETTE)
                .withCull(true)
                // 26.2 uses reversed-Z depth (buffer cleared to 0.0 = far, near = 1.0),
                // so "closer wins" is GREATER_THAN_OR_EQUAL — matches DepthStencilState.DEFAULT
                // and every vanilla pipeline. LESS_THAN_OR_EQUAL (the 26.1 value) rejects
                // every fragment here and the panes render nothing.
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .build();
    }

    public static final RenderType PRIDE_STRIPES_RENDER_TYPE = RenderType.create(
            CompactMachinesCore.id("pride_stripes"),
            RenderSetup.builder(PRIDE_STRIPES_PIPELINE)
                    .sortOnUpload()
                    .createRenderSetup());



    /**
     * Listener target for {@link RegisterRenderPipelinesEvent}. Idempotent —
     * safe to call again after a resource reload once the pipeline map is
     * itself rebuilt.
     */
    public static void registerAll(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PRIDE_STRIPES_PIPELINE);
    }
}
