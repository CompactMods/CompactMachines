package dev.compactmods.machines.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import dev.compactmods.machines.client.level.RenderingLevel;
import dev.compactmods.machines.client.render.RenderTypes;
import dev.compactmods.machines.client.util.TransformingVertexBuilder;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.ui.CompactMachineRoomMenu;
import dev.compactmods.machines.util.CompactStructureGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.HashMap;

public class MachineRoomScreen extends AbstractContainerScreen<CompactMachineRoomMenu> {

    protected double rotateX = 45.0f;
    protected double rotateY = 25.0f;

    public MachineRoomScreen(CompactMachineRoomMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 300;
        this.titleLabelY = 10;
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
        float mid =(this.imageWidth / 2f) - (font.width(this.title) / 2f);
        this.font.draw(pose, this.title, mid, (float)this.titleLabelY, FastColor.ARGB32.color(255, 255, 255, 255));
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

            final var blocks = new HashMap<BlockPos, BlockState>();

            BlockState solidWall = Registration.BLOCK_SOLID_WALL.get().defaultBlockState();

            for(Direction dir : Direction.Plane.HORIZONTAL) {
                var side = CompactStructureGenerator.getWallBounds(RoomSize.MAXIMUM, new BlockPos(8, 7, 8), dir);

                var closestHor1 = Direction.fromYRot(rotateX - 40);
                var closestHor = Direction.fromYRot(rotateX);
                var closestHor2 = Direction.fromYRot(rotateX + 40);
                if(dir == closestHor || dir == closestHor1 || dir == closestHor2)
                    continue;;

                BlockPos.betweenClosedStream(side)
                        .map(BlockPos::immutable)
                        .forEach(p -> {
                            blocks.putIfAbsent(p, solidWall);
                        });
            }


            var floor = CompactStructureGenerator.getWallBounds(RoomSize.MAXIMUM, new BlockPos(8, 7, 8), Direction.DOWN);
            BlockPos.betweenClosedStream(floor)
                    .map(BlockPos::immutable)
                    .forEach(p -> {
                        blocks.putIfAbsent(p, solidWall);
                    });

            blocks.put(new BlockPos(8, 0, 8), Blocks.REDSTONE_BLOCK.defaultBlockState());
            blocks.put(new BlockPos(8, 7, 8), Blocks.RED_STAINED_GLASS.defaultBlockState());

            BlockPos origin = BlockPos.ZERO.above();
            blocks.put(origin.above(0).south(4).east(4), Blocks.GOLD_BLOCK.defaultBlockState());
            blocks.put(origin.above(1).south(4).east(4), Blocks.REDSTONE_WIRE.defaultBlockState());

            blocks.put(origin.south(7).east(5), Blocks.IRON_BLOCK.defaultBlockState());
            blocks.put(origin.south(2).east(2), Blocks.COMMAND_BLOCK.defaultBlockState());

            final var renderer = new RenderingLevel(blocks);

            final float s = (RoomSize.MAXIMUM.getInternalSize() + 4) / 2f;
            pose.pushPose();
            {
                // pose.translate(s, s, s);

                pose.translate(getGuiLeft() + (getXSize() / 2d), getGuiTop() + 120, 150);

                pose.scale(9, -9, 9);

                pose.mulPose(Vector3f.XP.rotationDegrees((float) rotateY));
                pose.mulPose(Vector3f.YP.rotationDegrees((float) rotateX));
                pose.translate(-s, -s+1, -s);
                final var transformer = new TransformingVertexBuilder(buffer, RenderTypes.TRANSLUCENT_FULLBRIGHT);

                for (BlockPos pos : blocks.keySet()) {
                    pose.pushPose();
                    {
                        pose.translate(pos.getX(), pos.getY(), pos.getZ());

                        final var state = renderer.getBlockState(pos);
                        transformer.setOverlay(OverlayTexture.NO_OVERLAY);

                        blockRenderer.getModelRenderer().tesselateBlock(renderer, blockRenderer.getBlockModel(state), state,
                                pos, pose, transformer, false, renderer.random, state.getSeed(pos),
                                OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

                    }
                    pose.popPose();
                }
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
        AbstractContainerScreen.fill(pose, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0x99999999);
    }
}
