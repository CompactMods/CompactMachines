package org.dave.compactmachines3.gui.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.utility.ChunkUtils;
import org.dave.compactmachines3.utility.Logz;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMachine extends GuiContainer {

    protected static final int GUI_WIDTH = 256;
    protected static final int GUI_HEIGHT = 256;

    private int prevMouseX = -1;
    private int prevMouseY = -1;

    protected double rotateX = 0.0f;
    protected double rotateY = -25.0f;

    int glListId = -1;

    public GuiMachine() {
        super(new GuiMachineContainer());
        this.width = GUI_WIDTH;
        this.height = GUI_HEIGHT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(!GuiMachineData.canRender) {
            return;
        }

        if(GuiMachineData.requiresNewDisplayList) {
            TileEntityRendererDispatcher.instance.setWorld(GuiMachineData.proxyWorld);

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

            List<BlockPos> toRenderCopy = new ArrayList<>(GuiMachineData.toRender);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            GlStateManager.disableAlpha();
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.SOLID, toRenderCopy);
            GlStateManager.enableAlpha();
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT_MIPPED, toRenderCopy);
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT, toRenderCopy);
            GlStateManager.shadeModel(GL11.GL_FLAT);
            this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.TRANSLUCENT, toRenderCopy);

            tessellator.draw();


            GlStateManager.popMatrix();
            GlStateManager.popAttrib();

            GlStateManager.glEndList();
        }

        // TODO: Maybe add some other useful information to the screen
        if(GuiMachineData.chunk != null) {
            renderChunk();
        }
    }



    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        prevMouseX = mouseX;
        prevMouseY = mouseY;

        if(rotateX == 0.0d && mouseX > 84) {
            rotateX = RenderTickCounter.renderTicks * 45.0f / 128.0f;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if(prevMouseX != mouseX || prevMouseY != mouseY) {
            int relativeX = mouseX - prevMouseX;
            int relativeY = mouseY - prevMouseY;

            this.rotateX += relativeX;
            this.rotateY -= relativeY;

            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
    }

    public void renderChunk() {
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
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableCull();

        // Center on screen
        GlStateManager.translate(width / 2, height / 2, 180.0f);

        // Increase size a bit more at the end
        GlStateManager.scale(2.0f, 2.0f, 2.0f);

        GlStateManager.scale(-1.0f, 1.0f, 1.0f);

        // Tilt a bit
        GlStateManager.rotate((float)rotateY, 1.0f, 0.0f, 0.0f);

        // Turn it around
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, -1.0f);

        // Auto rotate
        GlStateManager.rotate(rotateX == 0.0d ? RenderTickCounter.renderTicks * 45.0f / 128.0f : (float)rotateX, 0.0f, 1.0f, 0.0f);

        // Get rid of the wall+floor
        GlStateManager.translate(-8.0f, -8.0f, -8.0f);

        // Now center at the middle (8 * inside offset)
        // 7x7x7 -> 5 -> 2.5 * 8 = 20.0f
        float shift = (GuiMachineData.machineSize-1) * -4.0f;
        GlStateManager.translate(shift, shift, shift);

        GlStateManager.scale(8.0f, 8.0f, 8.0f);

        // Aaaand render
        GlStateManager.callList(glListId);

        GlStateManager.resetColor();

        if(ConfigurationHandler.MachineSettings.renderTileEntitiesInGUI) {
            this.renderTileEntities(TileEntityRendererDispatcher.instance, new ArrayList<>(GuiMachineData.toRender));
        }

        if(ConfigurationHandler.MachineSettings.renderLivingEntitiesInGUI) {
            this.renderEntities();
        }

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
    }

    public void renderLayer(BlockRendererDispatcher blockrendererdispatcher, BufferBuilder buffer, BlockRenderLayer renderLayer, List<BlockPos> toRender) {
        IBlockAccess blockAccess = ChunkUtils.getBlockAccessFromChunk(GuiMachineData.chunk);
        for (BlockPos pos : toRender) {
            IBlockState state = blockAccess.getBlockState(pos);

            if (!state.getBlock().canRenderInLayer(state, renderLayer)) {
                continue;
            }

            try {
                state = state.getActualState(blockAccess, pos);
            } catch (Exception e) {
                Logz.debug("Could not determine actual state of block: %s", state.getBlock());
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

    private void renderEntities() {
        ClassInheritanceMultiMap<Entity> entities = GuiMachineData.chunk.getEntityLists()[2];
        for(Entity entity : entities) {
            renderEntity(entity);
        }
    }

    private static void renderEntity(Entity entity) {
        GlStateManager.pushMatrix();

        double x = entity.posX % 1024;
        double y = entity.posY - 40;
        double z = entity.posZ;

        RenderHelper.enableStandardItemLighting();

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, x, y, z, entity.rotationYaw, 1.0F, false);
        } catch (Exception e) {
            Logz.debug("Could not render entity '%s': %s", entity.getClass().getSimpleName(), e.getMessage());
        }

        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    private void renderTileEntities(TileEntityRendererDispatcher renderer, List<BlockPos> toRender) {
        ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
        IBlockAccess blockAccess = ChunkUtils.getBlockAccessFromChunk(GuiMachineData.chunk);

        for (BlockPos pos : toRender) {
            TileEntity te = blockAccess.getTileEntity(pos);
            if(te != null) {
                te.setWorld(GuiMachineData.proxyWorld);
                te.setPos(pos);

                if(te instanceof ITickable) {
                    ((ITickable) te).update();
                }

                GlStateManager.pushMatrix();
                GlStateManager.pushAttrib();
                renderer.renderEngine = Minecraft.getMinecraft().renderEngine;

                renderer.preDrawBatch();
                try {
                    renderer.render(te, pos.getX(), pos.getY(), pos.getZ(), 0.0f);
                } catch(Exception e) {
                    Logz.warn("Could not render tile entity '%s': %s", te.getClass().getSimpleName(), e.getMessage());
                }
                renderer.drawBatch(0);

                GlStateManager.popAttrib();
                GlStateManager.popMatrix();
            }
        }

        ForgeHooksClient.setRenderLayer(null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
