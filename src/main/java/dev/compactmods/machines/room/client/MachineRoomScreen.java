package dev.compactmods.machines.room.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.client.gui.widget.PSDIconButton;
import dev.compactmods.machines.client.level.RenderingLevel;
import dev.compactmods.machines.client.render.RenderTypes;
import dev.compactmods.machines.client.render.SuperRenderTypeBuffer;
import dev.compactmods.machines.client.util.TransformingVertexBuilder;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import dev.compactmods.machines.room.network.PlayerStartedRoomTrackingPacket;
import dev.compactmods.machines.room.network.RoomNetworkHandler;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.client.model.data.ModelData;

public class MachineRoomScreen extends AbstractContainerScreen<MachineRoomMenu> {

    private final Inventory inv;
    protected double rotateX = 45.0f;
    protected double rotateY = 20.0f;
    private PSDIconButton psdButton;
    private RenderingLevel renderer;

    public MachineRoomScreen(MachineRoomMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 248;
        this.imageHeight = 239;
        this.titleLabelY = 5;
        this.inv = inv;

        // Send packet to server for block data
        RoomNetworkHandler.CHANNEL.sendToServer(new PlayerStartedRoomTrackingPacket(menu.getRoom()));
        updateBlockRender();
    }

    @Override
    protected void init() {
        super.init();

        this.psdButton = addRenderableWidget(new PSDIconButton(this, leftPos + 220, topPos + 210));
        if (hasPsdItem())
            this.psdButton.setEnabled(true);
    }

    public void updateBlockRender() {
        var struct = menu.getBlocks();
        this.renderer = new RenderingLevel(struct);
    }

    private boolean hasPsdItem() {
        return inv.contains(new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        psdButton.setEnabled(this.inv.player.isCreative() || hasPsdItem());
        renderer.tbe();
    }

    @Override
    public boolean mouseDragged(double mx, double my, int mButton, double dx, double dy) {
        var s = super.mouseDragged(mx, my, mButton, dx, dy);
        if (!s) return false;

        rotateX += dx;
        rotateY += dy;
        return true;
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        pose.pushPose();
        pose.translate(0, 0, 500);

        pose.translate(this.imageWidth / 2f, 0, 0);

        var p = Component.literal(menu.getRoomName());
        Screen.drawCenteredString(pose, font, p, 0, this.titleLabelY, 0xFFFFFFFF);

        var room = menu.getRoom();
        var rt =  Component.literal("(%s, %s)".formatted(room.x, room.z));
        pose.scale(0.8f, 0.8f, 0.8f);
        Screen.drawCenteredString(pose, font, rt, 0,this.titleLabelY + font.lineHeight + 2, 0xFFCCCCCC);
        pose.popPose();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partial);

        var buffer = SuperRenderTypeBuffer.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.backupProjectionMatrix();

        // has to be outside of MS transforms, important for vertex sorting
        Matrix4f matrix4f = new Matrix4f(RenderSystem.getProjectionMatrix());
        matrix4f.multiplyWithTranslation(0, 0, 800);
        RenderSystem.setProjectionMatrix(matrix4f);

        PoseStack.Pose lastEntryBeforeTry = pose.last();

        var cam = minecraft.cameraEntity;


        try {
            pose.pushPose();
            pose.translate(0, 0, -800);

            final var blockRenderer = Minecraft.getInstance().getBlockRenderer();
            final var beRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();

            var struct = menu.getBlocks();

            pose.pushPose();
            {
                // pose.translate(s, s, s);

                pose.translate(getGuiLeft() + (getXSize() / 2d), getGuiTop() + 135, 150);

                float zoom = switch (struct.getSize().getX()) {
                    case 3 -> 23.5f;
                    case 5 -> 19.5f;
                    case 7 -> 15.5f;
                    case 9 -> 14.5f;
                    case 11 -> 11.5f;
                    case 13 -> 10.5f;
                    default -> 10.5f;
                };

                pose.scale(zoom, -zoom, zoom);

                pose.mulPose(Vector3f.XP.rotationDegrees((float) rotateY));
                pose.mulPose(Vector3f.YP.rotationDegrees((float) rotateX));

                final var tSize = struct.getSize();
                final float s = tSize.getX() / 2f;
                pose.translate(-s, -s + 1, -s);

                final var transformer = new TransformingVertexBuilder(buffer, RenderTypes.WALLS);

                var bb = struct.getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO);

                var as = new ArmorStand(renderer, 0, 0, 0);
                minecraft.cameraEntity = as;

                BlockPos.betweenClosedStream(bb).forEach(pos -> {
                    pose.pushPose();
                    {
                        pose.translate(pos.getX(), pos.getY(), pos.getZ());

                        final var state = renderer.getBlockState(pos);
                        transformer.setOverlay(OverlayTexture.RED_OVERLAY_V);

                        ModelData modelData = ModelData.EMPTY;
                        if (state.hasBlockEntity()) {
                            final var be = renderer.getBlockEntity(pos);
                            if (be != null) {
                                modelData = be.getModelData();
                                final var ber = beRenderer.getRenderer(be);
                                if (ber != null) {
                                    ber.render(be, 1f, pose, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                                }
                            }
                        }

                        try {
                            pose.pushPose();

                            for(var type : blockRenderer.getBlockModel(state).getRenderTypes(state, minecraft.level.random, modelData)) {
                                blockRenderer.renderBatched(state, pos, renderer, pose, buffer.getBuffer(type), true, renderer.random, modelData, type);
                            }

                            pose.popPose();
                        } catch (Exception e) {
                        }
                    }
                    pose.popPose();
                });
            }
            pose.popPose();
            pose.popPose();
        } catch (Exception e) {
            while (lastEntryBeforeTry != pose.last())
                pose.popPose();
        }

        minecraft.cameraEntity = cam;

        buffer.draw();
        RenderSystem.restoreProjectionMatrix();
    }

    @Override
    protected void renderBg(PoseStack pose, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShaderTexture(0, new ResourceLocation(Constants.MOD_ID, "textures/gui/room_menu.png"));

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        // this.blit(pose, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    public LevelBlockPosition getMachine() {
        return menu.getMachine();
    }
}
