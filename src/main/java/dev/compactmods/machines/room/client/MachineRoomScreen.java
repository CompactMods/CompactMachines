package dev.compactmods.machines.room.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.client.gui.widget.PSDIconButton;
import dev.compactmods.machines.client.level.RenderingLevel;
import dev.compactmods.machines.client.render.RenderTypes;
import dev.compactmods.machines.client.util.TransformingVertexBuilder;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import dev.compactmods.machines.room.network.PlayerStartedRoomTrackingPacket;
import dev.compactmods.machines.room.network.RoomNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public class MachineRoomScreen extends AbstractContainerScreen<MachineRoomMenu> {

    private final Inventory inv;
    protected double rotateX = 45.0f;
    protected double rotateY = 20.0f;
    private PSDIconButton psdButton;

    public MachineRoomScreen(MachineRoomMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 248;
        this.imageHeight = 239;
        this.titleLabelY = 5;
        this.inv = inv;

        // Send packet to server for block data
        RoomNetworkHandler.CHANNEL.sendToServer(new PlayerStartedRoomTrackingPacket(inv.player.getUUID(), menu.getRoom()));
    }

    @Override
    protected void init() {
        super.init();

        this.psdButton = addRenderableWidget(new PSDIconButton(this, leftPos + 220, topPos + 210));
        if(hasPsdItem())
            this.psdButton.setEnabled(true);
    }

    private boolean hasPsdItem() {
        return inv.contains(new ItemStack(Registration.PERSONAL_SHRINKING_DEVICE.get()));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        psdButton.setEnabled(hasPsdItem());
    }

    @Override
    public boolean mouseDragged(double mx, double my, int mButton, double dx, double dy) {
        var s = super.mouseDragged(mx, my, mButton, dx, dy);
        if(!s) return false;

        rotateX += dx;
        rotateY += dy;
        return true;
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        pose.pushPose();
        pose.translate(0, 0, 500);
        float mid =(this.imageWidth / 2f) - (font.width("Room Preview") / 2f);
        this.font.draw(pose, new TextComponent("Room Preview"), mid, (float)this.titleLabelY, 0x00000000);
        pose.popPose();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partial);

        final var buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        PoseStack.Pose lastEntryBeforeTry = pose.last();

        try {
            pose.pushPose();

            final var blockRenderer = Minecraft.getInstance().getBlockRenderer();

            final var struct = menu.getBlocks();
            final var renderer = new RenderingLevel(struct);

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

                final var tSize = menu.getBlocks().getSize();
                final float s = tSize.getX() / 2f;
                pose.translate(-s, -s+1, -s);

                final var transformer = new TransformingVertexBuilder(buffer, RenderTypes.TRANSLUCENT_FULLBRIGHT);

                var bb = struct.getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO);


                BlockPos.betweenClosedStream(bb).forEach(pos -> {
                    pose.pushPose();
                    {
                        pose.translate(pos.getX(), pos.getY(), pos.getZ());

                        final var state = renderer.getBlockState(pos);
                        transformer.setOverlay(OverlayTexture.NO_OVERLAY);

                        IModelData modelData = EmptyModelData.INSTANCE;
                        if(state.hasBlockEntity()) {
                            final var be = renderer.getBlockEntity(pos);
                            if(be != null)
                                modelData = be.getModelData();
                        }

                        blockRenderer.getModelRenderer().tesselateBlock(renderer, blockRenderer.getBlockModel(state), state,
                                pos, pose, transformer, false, renderer.random, state.getSeed(pos),
                                OverlayTexture.NO_OVERLAY, modelData);

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

        buffer.endBatch();
    }

    @Override
    protected void renderBg(PoseStack pose, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShaderTexture(0, new ResourceLocation(CompactMachines.MOD_ID, "textures/gui/room_menu.png"));

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pose, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
