package dev.compactmods.machines.neoforge.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;

public record NineSliceRenderer(ResourceLocation texture, ScreenRectangle area, int sliceWidth, int sliceHeight,
                                int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight, int cornerWidth,
                                int cornerHeight, int edgeWidth, int edgeHeight) {

    public static Builder builder(ResourceLocation texture) {
        return new Builder(texture);
    }

    public void render(GuiGraphics graphics) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("blit setup");
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = graphics.pose().last().pose();
        profiler.pop();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        profiler.push("blitting");

        int x = area.position().x();
        int y = area.position().y();

        if (area.width() == uWidth && area.height() == vHeight) {
            blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, area.width(), area.height(), textureWidth, textureHeight);
        } else if (area.height() == vHeight) {
            blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, cornerWidth, area.height(), textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x + cornerWidth, y, area.width() - edgeWidth - cornerWidth, area.height(), uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, vHeight, textureWidth, textureHeight);
            blit(bufferbuilder, matrix4f, x + area.width() - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, area.height(), textureWidth, textureHeight);
        } else if (area.width() == uWidth) {
            blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, area.width(), cornerHeight, textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x, y + cornerHeight, area.width(), area.height() - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, uWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blit(bufferbuilder, matrix4f, x, y + area.height() - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, area.width(), edgeHeight, textureWidth, textureHeight);
        } else {
            blit(bufferbuilder, matrix4f, x, y, uOffset, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x + cornerWidth, y, area.width() - edgeWidth - cornerWidth, cornerHeight, uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, cornerHeight, textureWidth, textureHeight);
            blit(bufferbuilder, matrix4f, x + area.width() - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, cornerHeight, textureWidth, textureHeight);
            blit(bufferbuilder, matrix4f, x, y + area.height() - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, cornerWidth, edgeHeight, textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x + cornerWidth, y + area.height() - edgeHeight, area.width() - edgeWidth - cornerWidth, edgeHeight, uOffset + cornerWidth, vOffset + vHeight - edgeHeight, uWidth - edgeWidth - cornerWidth, edgeHeight, textureWidth, textureHeight);
            blit(bufferbuilder, matrix4f, x + area.width() - edgeWidth, y + area.height() - edgeHeight, uOffset + uWidth - edgeWidth, vOffset + vHeight - edgeHeight, edgeWidth, edgeHeight, textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x, y + cornerHeight, cornerWidth, area.height() - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x + cornerWidth, y + cornerHeight, area.width() - edgeWidth - cornerWidth, area.height() - edgeHeight - cornerHeight, uOffset + cornerWidth, vOffset + cornerHeight, uWidth - edgeWidth - cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blitRepeating(bufferbuilder, matrix4f, x + area.width() - edgeWidth, y + cornerHeight, cornerWidth, area.height() - edgeHeight - cornerHeight, uOffset + uWidth - edgeWidth, vOffset + cornerHeight, edgeWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
        }
        profiler.pop();

        profiler.push("drawing");
        //cachedBuffer.bind();
        BufferUploader.drawWithShader(bufferbuilder.end());

        profiler.pop();
    }

    private static void blit(BufferBuilder bufferbuilder, Matrix4f matrix4f, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
        bufferbuilder.vertex(matrix4f, (float) pX, (float) pY, (float) 0).uv((pUOffset + 0.0F) / (float) pTextureWidth, (pVOffset + 0.0F) / (float) pTextureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, (float) pX, (float) (pY + pHeight), (float) 0).uv((pUOffset + 0.0F) / (float) pTextureWidth, (pVOffset + (float) pHeight) / (float) pTextureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, (float) (pX + pWidth), (float) (pY + pHeight), (float) 0).uv((pUOffset + (float) pWidth) / (float) pTextureWidth, (pVOffset + (float) pHeight) / (float) pTextureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, (float) (pX + pWidth), (float) pY, (float) 0).uv((pUOffset + (float) pWidth) / (float) pTextureWidth, (pVOffset + 0.0F) / (float) pTextureHeight).endVertex();
    }

    private static void blitRepeating(BufferBuilder bufferbuilder, Matrix4f matrix4f, int pX, int pY, int pWidth, int pHeight, int pUOffset, int pVOffset, int pSourceWidth, int pSourceHeight, int textureWidth, int textureHeight) {
        int i = pX;

        int j;
        for (IntIterator intiterator = slices(pWidth, pSourceWidth); intiterator.hasNext(); i += j) {
            j = intiterator.nextInt();
            int k = (pSourceWidth - j) / 2;
            int l = pY;

            int i1;
            for (IntIterator intiterator1 = slices(pHeight, pSourceHeight); intiterator1.hasNext(); l += i1) {
                i1 = intiterator1.nextInt();
                int j1 = (pSourceHeight - i1) / 2;
                blit(bufferbuilder, matrix4f, i, l, pUOffset + k, pVOffset + j1, j, i1, textureWidth, textureHeight);
            }
        }
    }

    /**
     * Returns an iterator for dividing a value into slices of a specified size.
     * <p>
     *
     * @param pTarget the value to be divided.
     * @param pTotal  the size of each slice.
     * @return An iterator for iterating over the slices.
     */
    private static IntIterator slices(int pTarget, int pTotal) {
        int i = Mth.positiveCeilDiv(pTarget, pTotal);
        return new Divisor(pTarget, i);
    }

    public static class Builder {
        private final ResourceLocation texture;

        private ScreenRectangle area;

        private int sliceWidth;
        private int sliceHeight;
        private int uWidth;
        private int vHeight;
        private int uOffset;
        private int vOffset;
        private int textureWidth;
        private int textureHeight;

        public Builder(ResourceLocation texture) {
            this.texture = texture;
            this.area = ScreenRectangle.empty();
        }

        public Builder sliceSize(int width, int height) {
            this.sliceWidth = width;
            this.sliceHeight = height;
            return this;
        }

        public Builder area(int x, int y, int width, int height) {
            this.area = new ScreenRectangle(x, y, width, height);
            return this;
        }

        public Builder area(ScreenRectangle area) {
            this.area = area;
            return this;
        }

        public Builder uv(int uWidth, int vHeight) {
            return uv(uWidth, vHeight, 0, 0);
        }

        private Builder uv(int uWidth, int vHeight, int uOffset, int vOffset) {
            this.uWidth = uWidth;
            this.uOffset = uOffset;
            this.vHeight = vHeight;
            this.vOffset = vOffset;
            return this;
        }

        public Builder textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public NineSliceRenderer build() {
            int cornerWidth = sliceWidth;
            int cornerHeight = sliceHeight;
            int edgeWidth = sliceWidth;
            int edgeHeight = sliceHeight;
            cornerWidth = Math.min(cornerWidth, area.width() / 2);
            edgeWidth = Math.min(edgeWidth, area.width() / 2);
            cornerHeight = Math.min(cornerHeight, area.height() / 2);
            edgeHeight = Math.min(edgeHeight, area.height() / 2);

            return new NineSliceRenderer(texture, area, sliceWidth, sliceHeight, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight,
                    cornerWidth, cornerHeight, edgeWidth, edgeHeight);
        }
    }
}
