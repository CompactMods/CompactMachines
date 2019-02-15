package org.dave.compactmachines3.gui.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.gui.GUIHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.network.MessagePlayerWhiteListToggle;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.utility.ChunkUtils;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.utility.ShrinkingDeviceUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMachine extends GuiContainer {
    protected ResourceLocation tabIcons;

    private int windowWidth = 200;
    private int windowHeight = 212;

    private int prevMouseX = -1;
    private int prevMouseY = -1;

    protected double rotateX = 0.0f;
    protected double rotateY = -25.0f;

    private GuiTextField guiWhiteListInput;
    private GuiMachinePlayerWhitelist guiWhiteList;
    private GuiButton guiWhiteListAddButton;
    private GuiCheckBox guiMachineLockedButton;
    private GuiButton guiEnterButton;

    int glListId = -1;
    int activeTab = 0;  // TODO: This should not be an integer, but rather a GuiTab object or something like that

    public GuiMachine() {
        super(new GuiMachineContainer());
    }

    private boolean shouldShowTabs() {
        boolean isOwner = mc.player.getName().equals(GuiMachineData.owner);

        // TODO: Add server-side operator & isCreative check
        return isOwner;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.tabIcons = new ResourceLocation(CompactMachines3.MODID, "textures/gui/tabicons.png");

        int offsetX = (int)((this.width - this.windowWidth) / 2.0f);
        int offsetY = (int)((this.height - this.windowHeight) / 2.0f);

        this.buttonList.clear();
        this.guiWhiteListAddButton = new GuiButton(0, offsetX+5+windowWidth-30, offsetY+44, 20, 20, "+");
        this.buttonList.add(this.guiWhiteListAddButton);

        this.guiMachineLockedButton = new GuiCheckBox(1, offsetX+7, offsetY + 7, "", GuiMachineData.locked);
        this.buttonList.add(this.guiMachineLockedButton);

        this.guiWhiteListInput = new GuiTextField(0, this.fontRenderer, offsetX+6, offsetY+45, windowWidth-33, 18);

        this.guiWhiteList = new GuiMachinePlayerWhitelist(this,
                offsetY+65, offsetY+windowHeight - 5,
                offsetX+5,
                20,
                windowWidth - 10,
                windowHeight);

        this.guiEnterButton = new GuiButton(2, offsetX+5+windowWidth-30, offsetY+windowHeight-25, 20, 20, "");
        this.buttonList.add(this.guiEnterButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Do not draw the buttons automatically
        List<GuiButton> buttonListTemp = this.buttonList;
        this.buttonList = new ArrayList<>();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.buttonList = buttonListTemp;

        if(!GuiMachineData.canRender) {
            return;
        }

        List<BlockPos> toRenderCopy = CompactMachines3.clientWorldData.worldClone.providerClient.getRenderListForChunk(GuiMachineData.coords * 64, 0);
        if(GuiMachineData.requiresNewDisplayList && toRenderCopy != null) {
            TileEntityRendererDispatcher.instance.setWorld(CompactMachines3.clientWorldData.worldClone);

            if(glListId != -1) {
                GLAllocation.deleteDisplayLists(glListId);
            }

            glListId = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(glListId, GL11.GL_COMPILE);

            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();

            GlStateManager.translate(-GuiMachineData.coords*1024, -40, 0);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            // Aaaand render
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            //List<BlockPos> toRenderCopy = new ArrayList<>(GuiMachineData.toRender);


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

        if(CompactMachines3.clientWorldData.worldClone != null && activeTab == 0) {
            renderChunk(partialTicks);

            drawOwner(partialTicks, mouseX, mouseY);
            drawEnterButton(partialTicks, mouseX, mouseY);
        } else {
            // TODO: Draw unused screen and help information; account for future updates with loot compact machines
        }

        if(activeTab == 1) {
            drawWhitelist(partialTicks, mouseX, mouseY);
        }
    }

    private void drawEnterButton(float partialTicks, int mouseX, int mouseY) {
        if(!ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(mc.player)) {
            return;
        }

        if(GuiMachineData.coords == -1) {
            return;
        }

        if(!GuiMachineData.isAllowedToEnter(mc.player)) {
            return;
        }

        guiEnterButton.drawButton(this.mc, mouseX, mouseY, partialTicks);


        ItemStack itemstack = new ItemStack(Itemss.psd);
        GlStateManager.pushAttrib();
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI(itemstack, guiEnterButton.x+2, guiEnterButton.y+1);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();
    }

    protected void drawOwner(float partialTicks, int mouseX, int mouseY) {
        float offsetX = (this.width - this.windowWidth) / 2.0f;
        float offsetY = (this.height - this.windowHeight) / 2.0f;

        if(GuiMachineData.owner != null) {
            mc.fontRenderer.drawString(GuiMachineData.owner, offsetX + 8, offsetY + this.windowHeight - 16, 0xFF1f2429, false);
        } else {
            mc.fontRenderer.drawString(I18n.format("tooltip.compactmachines3.machine.coords.unused"), offsetX + 8, offsetY + this.windowHeight - 16, 0xFF1f2429, false);
        }
    }

    protected void drawWhitelist(float partialTicks, int mouseX, int mouseY) {
        guiWhiteListInput.drawTextBox();
        guiWhiteListAddButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        guiMachineLockedButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        guiWhiteList.drawScreen(mouseX, mouseY, partialTicks);

        int offsetX = (int)((this.width - this.windowWidth) / 2.0f);
        int offsetY = (int)((this.height - this.windowHeight) / 2.0f);

        fontRenderer.drawString("Lock for other players", offsetX+20, offsetY+9, 0x1f2429);
        fontRenderer.drawString("Whitelist:", offsetX+6, offsetY+34, 0x1f2429);

    }

    protected void drawTabs(float partialTicks, int mouseX, int mouseY) {
        if(!shouldShowTabs()) {
            return;
        }

        float offsetX = (this.width - this.windowWidth) / 2.0f;
        float offsetY = (this.height - this.windowHeight) / 2.0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX-28, offsetY-1, 0);

        mc.getTextureManager().bindTexture(tabIcons);

        GlStateManager.disableLighting();
        GlStateManager.color(1F, 1F, 1F); //Forge: Reset color in case Items change it.
        GlStateManager.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.

        for(int index = 0; index < 2; index++) {
            int xOffset = 0;
            int yOffset = index * 28;

            int buttonWidth = 32;
            if(activeTab != index) {
                buttonWidth = 28;
            }

            int textureY = index > 0 ? 28*2 : 28;

            drawTexturedModalRect(xOffset, yOffset+0, activeTab == index ? 32 : 0, textureY, buttonWidth, 28);
        }

        drawTexturedModalRect(10, 36, 64, 0, 12, 11);

        ItemStack itemstack = new ItemStack(Blockss.wall);
        GlStateManager.pushAttrib();
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI(itemstack, 8, 6);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();


        GlStateManager.popMatrix();
    }

    protected void drawWindow(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.renderEngine.bindTexture(tabIcons);

        float offsetX = (this.width - this.windowWidth) / 2.0f;
        float offsetY = (this.height - this.windowHeight) / 2.0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);

        int texOffsetY = 12;
        int texOffsetX = 64;

        // Top Left corner
        drawTexturedModalRect(0, 0, texOffsetX, texOffsetY, 4, 4);

        // Top right corner
        drawTexturedModalRect(this.windowWidth - 4, 0, texOffsetX + 4 + 64, texOffsetY, 4, 4);

        // Bottom Left corner
        drawTexturedModalRect(0, this.windowHeight - 4, texOffsetX, texOffsetY + 4 + 64, 4, 4);

        // Bottom Right corner
        drawTexturedModalRect(this.windowWidth - 4, this.windowHeight - 4, texOffsetX + 4 + 64, texOffsetY + 4 + 64, 4, 4);

        // Top edge
        GUIHelper.drawStretchedTexture(4, 0, this.windowWidth - 8, 4, texOffsetX + 4, texOffsetY, 64, 4);

        // Bottom edge
        GUIHelper.drawStretchedTexture(4, this.windowHeight - 4, this.windowWidth - 8, 4, texOffsetX + 4, texOffsetY + 4 + 64, 64, 4);

        // Left edge
        GUIHelper.drawStretchedTexture(0, 4, 4, this.windowHeight - 8, texOffsetX, texOffsetY+4, 4, 64);

        // Right edge
        GUIHelper.drawStretchedTexture(this.windowWidth - 4, 4, 4, this.windowHeight - 8, texOffsetX + 64 + 4, texOffsetY + 3, 4, 64);

        GUIHelper.drawStretchedTexture(4, 4, this.windowWidth - 8, this.windowHeight - 8, texOffsetX + 4, texOffsetY+4, 64, 64);

        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow(partialTicks, mouseX, mouseY);
        drawTabs(partialTicks, mouseX, mouseY);

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if(button.id == 0) {
            String playerName = this.guiWhiteListInput.getText();
            if(playerName.length() == 0) {
                return;
            }

            PackageHandler.instance.sendToServer(new MessagePlayerWhiteListToggle(GuiMachineData.coords, playerName));
            this.guiWhiteListInput.setText("");
        }

        if(button.id == 1) {
            PackageHandler.instance.sendToServer(new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TOGGLE_LOCKED));
        }

        if(button.id == 2) {
            PackageHandler.instance.sendToServer(new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TRY_TO_ENTER));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean typed = false;
        if(activeTab == 1) {
            typed = this.guiWhiteListInput.textboxKeyTyped(typedChar, keyCode);

            if (keyCode == 28 || keyCode == 156) {
                this.guiWhiteListAddButton.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(this.guiWhiteListAddButton);
            }
        }

        if (keyCode == 1 || (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode) && !typed)) {
            this.mc.player.closeScreen();
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        // Do not click the buttons automatically, they might be on a different tab
        List<GuiButton> buttonListTemp = this.buttonList;
        this.buttonList = new ArrayList<>();
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.buttonList = buttonListTemp;

        this.guiWhiteListInput.mouseClicked(mouseX, mouseY, mouseButton);

        if(activeTab == 1 && mouseButton == 0) {
            if (this.guiWhiteListAddButton.mousePressed(this.mc, mouseX, mouseY)) {
                this.guiWhiteListAddButton.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(this.guiWhiteListAddButton);
            }

            if(this.guiMachineLockedButton.mousePressed(this.mc, mouseX, mouseY)) {
                this.guiMachineLockedButton.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(this.guiMachineLockedButton);
            }
        } else if(activeTab == 0 && mouseButton == 0) {
            boolean mousePressed = this.guiEnterButton.mousePressed(this.mc, mouseX, mouseY);
            boolean hasDevice = ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(mc.player);
            boolean validCoords = GuiMachineData.coords != -1;
            boolean isAllowedToEnter = GuiMachineData.isAllowedToEnter(mc.player);

            if(mousePressed && hasDevice && validCoords && isAllowedToEnter) {
                this.guiEnterButton.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(this.guiEnterButton);
            }
        }

        if(shouldShowTabs()) {
            float offsetX = ((this.width - this.windowWidth) / 2.0f) - 28;
            float offsetY = (this.height - this.windowHeight) / 2.0f;

            for(int tabIndex = 0; tabIndex < 2; tabIndex++) {
                int tabY = (int)offsetY + (tabIndex * 28);

                if(mouseX < offsetX || mouseX > offsetX + 28) {
                    continue;
                }

                if(tabY < mouseY && mouseY < tabY + 28) {
                    this.activeTab = tabIndex;

                    if(tabIndex == 0) {
                        this.rotateX = 0.0d;
                        mouseX = 0;
                    }
                }
            }
        }

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

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if(activeTab == 1) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            if (this.guiWhiteList != null) {
                this.guiWhiteList.handleMouseInput(mouseX, mouseY);
            }
        }
    }

    public void renderChunk(float partialTicks) {
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
        GlStateManager.rotate((float)rotateY, 1.0f, 0.0f, 0.0f);

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

        List<BlockPos> toRenderCopy = CompactMachines3.clientWorldData.worldClone.providerClient.getRenderListForChunk(GuiMachineData.coords * 1024, 0);
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

        /*
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
        */
    }

    private void renderEntities() {
        ClassInheritanceMultiMap<Entity> entities = CompactMachines3.clientWorldData.worldClone.getChunk(GuiMachineData.coords * 16, 0).getEntityLists()[2];
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
