package org.dave.compactmachines3.gui.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.ChunkUtils;
import org.dave.compactmachines3.utility.Logz;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiMachine extends GuiScreen {

    protected static final int GUI_WIDTH = 256;
    protected static final int GUI_HEIGHT = 256;

    TileEntityMachine machine;

    int glListId = -1;

    public GuiMachine(TileEntityMachine machine) {
        this.machine = machine;

        this.width = GUI_WIDTH;
        this.height = GUI_HEIGHT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        if(GuiMachineChunkHolder.rawData != null && GuiMachineChunkHolder.chunk == null) {
            GuiMachineChunkHolder.chunk = ChunkUtils.readChunkFromNBT(mc.world, GuiMachineChunkHolder.rawData);
            IBlockAccess blockAccess = ChunkUtils.getBlockAccessFromChunk(GuiMachineChunkHolder.chunk);
            List<BlockPos> toRender = new ArrayList<>();
            for(int x = 15; x >= 0; x--) {
                for(int y = 15; y >= 0; y--) {
                    for(int z = 15; z >= 0; z--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if(blockAccess.isAirBlock(pos)) {
                            continue;
                        }

                        toRender.add(pos);
                    }
                }
            }

            if(glListId != -1) {
                GLAllocation.deleteDisplayLists(glListId);
            }

            glListId = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(glListId, GL11.GL_COMPILE);

            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // Aaaand render
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            GlStateManager.disableAlpha();
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.SOLID, toRender);
            GlStateManager.enableAlpha();
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT_MIPPED, toRender);
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT, toRender);
            GlStateManager.shadeModel(GL11.GL_FLAT);
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.TRANSLUCENT, toRender);

            tessellator.draw();

            GlStateManager.popMatrix();
            GlStateManager.popAttrib();

            GlStateManager.glEndList();
        }

        // TODO: Maybe add some other useful information to the screen
        if(GuiMachineChunkHolder.chunk != null) {
            renderChunk();
        }
    }

    public void renderChunk() {
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        // Init GlStateManager
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.enableCull();

        // Center on screen
        GlStateManager.translate(width / 2, height / 2, 180.0f);

        // Increase size a bit more at the end
        GlStateManager.scale(2.0f, 2.0f, 2.0f);

        // Tilt a bit
        GlStateManager.rotate(-25.0f, 1.0f, 0.0f, 0.0f);

        // Turn it around
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, -1.0f);

        // Auto rotate
        float angle = RenderTickCounter.renderTicks * 45.0f / 128.0f;
        GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);

        // Get rid of the wall+floor
        GlStateManager.translate(-8.0f, -8.0f, -8.0f);

        // Now center at the middle (8 * inside offset)
        // 7x7x7 -> 5 -> 2.5 * 8 = 20.0f
        float shift = (machine.getSize().getDimension()-1) * -4.0f;
        GlStateManager.translate(shift, shift, shift);

        GlStateManager.scale(8.0f, 8.0f, 8.0f);

        GL11.glFrontFace(GL11.GL_CW);

        // Aaaand render
        GlStateManager.callList(glListId);

        GL11.glFrontFace(GL11.GL_CCW);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
    }

    public void renderLayer(BlockRendererDispatcher blockrendererdispatcher, BufferBuilder buffer, BlockRenderLayer renderLayer, List<BlockPos> toRender) {
        IBlockAccess blockAccess = ChunkUtils.getBlockAccessFromChunk(GuiMachineChunkHolder.chunk);
        for (BlockPos pos : toRender) {
            IBlockState state = blockAccess.getBlockState(pos);

            if (!state.getBlock().canRenderInLayer(state, renderLayer)) {
                continue;
            }

            ForgeHooksClient.setRenderLayer(renderLayer);

            try {
                blockrendererdispatcher.renderBlock(state, pos, blockAccess, buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ForgeHooksClient.setRenderLayer(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
