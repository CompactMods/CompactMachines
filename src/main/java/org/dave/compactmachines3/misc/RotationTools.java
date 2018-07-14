package org.dave.compactmachines3.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.CompactMachines3;

import java.util.ArrayList;

public class RotationTools {
    @SideOnly(Side.CLIENT)
    private static ResourceLocation arrowImage;

    @SideOnly(Side.CLIENT)
    public static void renderArrowOnGround(Vec3d cameraPosition, Vec3d hitPosition, BlockPos drawPosition) {
        EnumFacing facing = RotationTools.getFacingByTriangle(hitPosition);

        RotationTools.TextureRotationList rotList = new RotationTools.TextureRotationList();
        switch (facing) {
            case SOUTH:
                break;
            case WEST:
                rotList.rotateFromStart();
                break;
            case NORTH:
                rotList.rotateFromStart();
                rotList.rotateFromStart();
                break;
            case EAST:
                rotList.rotateFromStart();
                rotList.rotateFromStart();
                rotList.rotateFromStart();
                break;
        }

        if(arrowImage == null) {
             arrowImage = new ResourceLocation(CompactMachines3.MODID, "textures/particles/blockmarker.png");
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(arrowImage);

        GlStateManager.pushMatrix();

        GlStateManager.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        GlStateManager.translate(drawPosition.getX(), drawPosition.getY(), drawPosition.getZ());

        // Draw with 50% transparency
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);

        // Actually draw
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        rotList.fillBufferBuilder(bufferbuilder, 0.0005d);

        tessellator.draw();

        GlStateManager.popMatrix();
    }

    public static EnumFacing getFacingByTriangle(Vec3d vec) {
        if(vec.z > 0) {
            if(vec.x < 0) {
                // Quadrant 1
                if(Math.abs(vec.x) < Math.abs(vec.z)) {
                    // Bottom
                    return EnumFacing.SOUTH;
                } else {
                    // Left
                    return EnumFacing.WEST;
                }
            } else {
                // Quadrant 2
                if(Math.abs(vec.x) > Math.abs(vec.z)) {
                    // Right
                    return EnumFacing.EAST;
                } else {
                    // Bottom
                    return EnumFacing.SOUTH;
                }
            }
        } else {
            if(vec.x < 0) {
                // Quadrant 3
                if(Math.abs(vec.x) < Math.abs(vec.z)) {
                    // Top
                    return EnumFacing.NORTH;
                } else {
                    // Left
                    return EnumFacing.WEST;
                }

            } else {
                // Quadrant 4
                if(Math.abs(vec.x) > Math.abs(vec.z)) {
                    // Right
                    return EnumFacing.EAST;
                } else {
                    // Top
                    return EnumFacing.NORTH;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class TextureRotationList extends RotatingList<Tuple<Integer, Integer>> {
        public TextureRotationList() {
            this.add(new Tuple<>(0, 1));
            this.add(new Tuple<>(1, 1));
            this.add(new Tuple<>(1, 0));
            this.add(new Tuple<>(0, 0));
        }

        public void fillBufferBuilder(BufferBuilder buffer, double yLevel) {
            buffer.pos(0, yLevel, 1).tex(this.get(0).getFirst(), this.get(0).getSecond()).endVertex();
            buffer.pos(1, yLevel, 1).tex(this.get(1).getFirst(), this.get(1).getSecond()).endVertex();
            buffer.pos(1, yLevel, 0).tex(this.get(2).getFirst(), this.get(2).getSecond()).endVertex();
            buffer.pos(0, yLevel, 0).tex(this.get(3).getFirst(), this.get(3).getSecond()).endVertex();

        }
    }


    public static class RotatingList<T> extends ArrayList<T> {
        public void rotateFromStart() {
            T firstElement = this.get(0);
            this.remove(0);

            this.add(firstElement);
        }

        public void rotateFromEnd() {
            T lastElement = this.get(this.size()-1);
            this.remove(this.size()-1);
            this.add(0, lastElement);
        }
    }
}
