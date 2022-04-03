package dev.compactmods.machines.compat.theoneprobe.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.client.level.RenderingLevel;
import dev.compactmods.machines.client.render.RenderTypes;
import dev.compactmods.machines.client.util.TransformingVertexBuilder;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.data.RoomPreview;
import mcjty.theoneprobe.api.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Collections;

public class RoomPreviewElement implements IElement {

    public static final ResourceLocation ID = new ResourceLocation(CompactMachines.MOD_ID, "room_preview");
    private final RoomPreview data;

    private StructureTemplate blocks;
    private RenderingLevel renderingLevel;

    public RoomPreviewElement(RoomPreview data) {
        this.data = data;
        this.renderingLevel = new RenderingLevel(Collections.emptyMap());
    }

    @Override
    public void render(PoseStack pose, int x, int y) {

        Font fr = Minecraft.getInstance().font;
        fr.draw(pose, "hi", x, y, 0xffff0000);

        try {
            if (blocks != null) {
                final var buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                final var blockRenderer = Minecraft.getInstance().getBlockRenderer();

                pose.pushPose();
                {
                    pose.translate(10, 10, 0);
                    pose.scale(5, 5, 1);
                    pose.mulPose(new Quaternion(0, 90, 0, true));

                    pose.pushPose();
                    {
                        final var state = Blocks.GOLD_BLOCK.defaultBlockState();

                        final var transformer = new TransformingVertexBuilder(buffer, RenderTypes.TRANSLUCENT_FULLBRIGHT);

                        blockRenderer.getModelRenderer().tesselateBlock(renderingLevel, blockRenderer.getBlockModel(state), state,
                                BlockPos.ZERO, pose, transformer, false, renderingLevel.random, state.getSeed(BlockPos.ZERO),
                                OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

                    }
                    pose.popPose();
                }

                pose.popPose();

                buffer.endBatch();
            }
        }
        catch(Exception e) {

        }
    }

    @Override
    public int getWidth() {
        return 128;
    }

    @Override
    public int getHeight() {
        return 128;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeWithCodec(RoomPreview.CODEC, this.data);
        buf.writeNbt(blocks.save(new CompoundTag()));
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public void loadBlocks(MinecraftServer server, CompactRoomData.RoomData data) {
        final var lev = server.getLevel(Registration.COMPACT_DIMENSION);
        if (lev == null)
            return;

        final var inBounds = data.getRoomBounds();
        blocks = new StructureTemplate();
        blocks.fillFromWorld(lev, new BlockPos(inBounds.minX - inBounds.getXsize(), inBounds.minY, inBounds.minZ - inBounds.getZsize()),
                new BlockPos(inBounds.getXsize() * 2, inBounds.getYsize(), inBounds.getZsize() * 2),
                false, null);

        this.renderingLevel = new RenderingLevel(blocks);
    }

    public void loadBlocks(CompoundTag nbt) {
        blocks = new StructureTemplate();
        blocks.load(nbt);

        this.renderingLevel = new RenderingLevel(blocks);
    }
}
