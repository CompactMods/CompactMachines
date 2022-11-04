package dev.compactmods.machines.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.client.CM4Shaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public class RenderTypes extends RenderStateShard {
	public static final VertexFormat BLOCK_WITH_OVERLAY = new VertexFormat(
			ImmutableMap.<String, VertexFormatElement>builder()
					.put("Position", ELEMENT_POSITION)
					.put("Color", ELEMENT_COLOR)
					.put("UV0", ELEMENT_UV0)
					.put("UV1", ELEMENT_UV1)
					.put("UV2", ELEMENT_UV2)
					.put("Normal", ELEMENT_NORMAL)
					.put("Padding", ELEMENT_PADDING)
					.build()
	);

	protected static final RenderStateShard.LightmapStateShard LIGHTMAP_DISABLED = new RenderStateShard.LightmapStateShard(false);

	protected static final RenderStateShard.ShaderStateShard WALL_BLOCKS = new RenderStateShard.ShaderStateShard(CM4Shaders::wall);

	public static final RenderType WALLS = RenderType.create(
			Constants.MOD_ID + ":wall",
			BLOCK_WITH_OVERLAY, VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(WALL_BLOCKS)
					.setLightmapState(LIGHTMAP_DISABLED)
					.setOverlayState(OVERLAY)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.createCompositeState(false)
	);

	public RenderTypes(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
		super(p_110161_, p_110162_, p_110163_);
	}
}
