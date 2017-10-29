package org.dave.compactmachines3.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.misc.TextureStitchHandler;

@SideOnly(Side.CLIENT)
public class ParticleBlockMarker extends Particle {
    public ParticleBlockMarker(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn, 0.0D, 0.0D, 0.0D);

        this.setParticleTexture(TextureStitchHandler.blockMarker);
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.particleGravity = 0.0F;
        this.particleMaxAge = 160;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float f = this.particleTexture.getMinU();
        float f1 = this.particleTexture.getMaxU();
        float f2 = this.particleTexture.getMinV();
        float f3 = this.particleTexture.getMaxV();

        float x = (float)this.posX - (float)interpPosX;
        float y = (float)this.posY - (float)interpPosY;
        float z = (float)this.posZ - (float)interpPosZ;

        // Bounce up and down
        y += 0.2d + Math.sin(RenderTickCounter.renderTicks / 60.0d) * 0.2d;

        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;

        buffer.pos((double)(x - 0.5F), (double)(y - 0.5F), (double)(z - 0.5F)).tex((double)f1, (double)f3).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
        buffer.pos((double)(x - 0.5F), (double)(y + 0.5F), (double)(z - 0.5F)).tex((double)f1, (double)f2).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
        buffer.pos((double)(x + 0.5F), (double)(y + 0.5F), (double)(z + 0.5F)).tex((double)f, (double)f2).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
        buffer.pos((double)(x + 0.5F), (double)(y - 0.5F), (double)(z + 0.5F)).tex((double)f, (double)f3).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();

        buffer.pos((double)(x + 0.5F), (double)(y - 0.5F), (double)(z - 0.5F)).tex((double)f1, (double)f3).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
        buffer.pos((double)(x + 0.5F), (double)(y + 0.5F), (double)(z - 0.5F)).tex((double)f1, (double)f2).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
        buffer.pos((double)(x - 0.5F), (double)(y + 0.5F), (double)(z + 0.5F)).tex((double)f, (double)f2).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
        buffer.pos((double)(x - 0.5F), (double)(y - 0.5F), (double)(z + 0.5F)).tex((double)f, (double)f3).color(1.0f, 1.0f, 1.0f, 0.8F).lightmap(b1, b2).endVertex();
    }
}
