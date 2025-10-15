package dev.compactmods.machines.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

public class RoomCoreModel implements SpecialModelRenderer<RoomTemplate> {

    protected static final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void submit(@Nullable RoomTemplate argument, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {

        poseStack.pushPose();

        poseStack.mulPose(Axis.XN.rotationDegrees(45f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45f));

        poseStack.scale(.25f, .25f, .25f);
        poseStack.translate(3.50D, 0.0D, 0.0D);

        nodeCollector.submitBlock(poseStack, Machines.Blocks.UNBOUND_MACHINE.get().defaultBlockState(),
                packedLight, packedOverlay, outlineColor);

        poseStack.pushPose();
        poseStack.translate(-2.0D, 0.0D, 0.0D);
        nodeCollector.submitBlock(poseStack, Rooms.Blocks.BREAKABLE_WALL.get().defaultBlockState(),
                packedLight, packedOverlay, outlineColor);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public void getExtents(Set<Vector3f> output) {

    }

    @Override
    public @Nullable RoomTemplate extractArgument(ItemStack stack) {
        return null;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        public Unbaked() {

        }

        @Override
        public @Nullable SpecialModelRenderer<?> bake(BakingContext context) {
            return new RoomCoreModel();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
