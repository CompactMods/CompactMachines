package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
import net.minecraftforge.client.ForgeHooksClient;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.MouseClickMoveEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;
import org.dave.compactmachines3.gui.framework.widgets.Widget;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.utility.Logz;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class WidgetMachinePreview extends Widget {
    protected double rotateX = 0.0f;
    protected double rotateY = 25.0f;

    private int prevMouseX = -1;
    private int prevMouseY = -1;

    int glListId = -1;

    long lastClickTime = Long.MAX_VALUE;

    public WidgetMachinePreview() {

        this.addListener(MouseClickEvent.class, (event, widget) -> {
            if(rotateX == 0.0d) {
                rotateX = RenderTickCounter.renderTicks * 45.0f / 128.0f;
            }


            return WidgetEventResult.CONTINUE_PROCESSING;
        });

        this.addListener(MouseClickMoveEvent.class, (event, widget) -> {
            int mouseX = event.x;
            int mouseY = event.y;

            if(event.timeSinceLastClick < lastClickTime) {
                prevMouseX = mouseX;
                prevMouseY = mouseY;
            }

            if(prevMouseX != mouseX || prevMouseY != mouseY) {
                int relativeX = mouseX - prevMouseX;
                int relativeY = mouseY - prevMouseY;

                this.rotateX += relativeX;
                this.rotateY += relativeY;

                prevMouseX = mouseX;
                prevMouseY = mouseY;
            }

            lastClickTime = event.timeSinceLastClick;

            return WidgetEventResult.HANDLED;
        });
    }

    @Override
    public void draw(GuiScreen screen) {
        super.draw(screen);

        if(!GuiMachineData.canRender) {
            return;
        }

        if(GuiMachineData.requiresNewDisplayList) {
            List<BlockPos> toRenderCopy = CompactMachines3.clientWorldData.worldClone.providerClient.getRenderListForChunk(GuiMachineData.coords * 64, 0);
            if(toRenderCopy != null) {
                TileEntityRendererDispatcher.instance.setWorld(CompactMachines3.clientWorldData.worldClone);

                if (glListId != -1) {
                    GLAllocation.deleteDisplayLists(glListId);
                }

                glListId = GLAllocation.generateDisplayLists(1);
                GlStateManager.glNewList(glListId, GL11.GL_COMPILE);

                GlStateManager.pushAttrib();
                GlStateManager.pushMatrix();

                GlStateManager.translate(-GuiMachineData.coords * 1024, -40, 0);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                // Aaaand render
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

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
        }

        if(CompactMachines3.clientWorldData.worldClone != null) {
            renderChunk();
        } else {
            // TODO: Draw unused screen and help information; account for future updates with loot compact machines
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

        double scaleToWindow = 1.0d / GuiMachineData.machineSize;
        scaleToWindow *= 8.0d;
        GlStateManager.scale(scaleToWindow, scaleToWindow, scaleToWindow);

        // Increase size a bit more at the end
        GlStateManager.scale(2.0f, 2.0f, 2.0f);

        GlStateManager.scale(-1.0f, 1.0f, 1.0f);

        // Tilt a bit
        GlStateManager.rotate((float)-rotateY, 1.0f, 0.0f, 0.0f);

        // Turn it around
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, -1.0f);

        // Auto rotate
        /*
        int rotationTime = 120; // 6 seconds to rotate one time
        int rotationTicks = (int) (Minecraft.getMinecraft().world.getWorldTime() % rotationTime * 8);

        float percent = ((rotationTicks / 8.0f) + partialTicks) / rotationTime;
        */

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

        List<BlockPos> toRenderCopy = CompactMachines3.clientWorldData.worldClone.providerClient.getRenderListForChunk(GuiMachineData.coords * 64, 0);
        if(ConfigurationHandler.MachineSettings.renderTileEntitiesInGUI) {
            this.renderTileEntities(TileEntityRendererDispatcher.instance, toRenderCopy);
        }

        if(ConfigurationHandler.MachineSettings.renderLivingEntitiesInGUI) {
            this.renderEntities();
            GlStateManager.enableBlend();
        }

        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
    }

    public void renderLayer(BlockRendererDispatcher blockrendererdispatcher, BufferBuilder buffer, BlockRenderLayer renderLayer, List<BlockPos> toRender) {
        for (BlockPos pos : toRender) {
            IBlockState state = CompactMachines3.clientWorldData.worldClone.getBlockState(pos);

            if (!state.getBlock().canRenderInLayer(state, renderLayer)) {
                continue;
            }

            try {
                state = state.getActualState(CompactMachines3.clientWorldData.worldClone, pos);
            } catch (Exception e) {
                Logz.debug("Could not determine actual state of block: %s", state.getBlock());
            }

            ForgeHooksClient.setRenderLayer(renderLayer);

            try {
                blockrendererdispatcher.renderBlock(state, pos, CompactMachines3.clientWorldData.worldClone, buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ForgeHooksClient.setRenderLayer(null);
        }
    }

    private void renderEntities() {
        if(CompactMachines3.clientWorldData == null || CompactMachines3.clientWorldData.worldClone == null) {
            return;
        }

        ClassInheritanceMultiMap<Entity> entities = CompactMachines3.clientWorldData.worldClone.getChunk(GuiMachineData.coords * 64, 0).getEntityLists()[2];
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
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, x, y, z, entity.rotationYaw, 1.0F, false);
        } catch (Exception e) {
            Logz.debug("Could not render entity '%s': %s", entity.getClass().getSimpleName(), e.getMessage());
        }

        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    private void renderTileEntities(TileEntityRendererDispatcher renderer, List<BlockPos> toRender) {
        if(toRender == null) {
            return;
        }
        ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
        for (BlockPos pos : toRender) {
            TileEntity te = CompactMachines3.clientWorldData.worldClone.getTileEntity(pos);
            if(te != null) {
                te.setWorld(CompactMachines3.clientWorldData.worldClone);
                te.setPos(pos);

                if (te instanceof ITickable) {
                    try {
                        ((ITickable) te).update();
                    } catch (Exception e) {
                    }
                }

                GlStateManager.pushMatrix();
                GlStateManager.pushAttrib();
                renderer.renderEngine = Minecraft.getMinecraft().renderEngine;

                renderer.preDrawBatch();
                try {
                    renderer.render(te, pos.getX() % 1024, pos.getY()-40, pos.getZ(), 0.0f);
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
}
