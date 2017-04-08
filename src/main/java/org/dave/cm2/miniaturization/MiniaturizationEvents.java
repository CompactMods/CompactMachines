package org.dave.cm2.miniaturization;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.cm2.init.Itemss;
import org.dave.cm2.init.Potionss;


public class MiniaturizationEvents {
    /*
     * Render living entities smaller according to their scale attribute
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onLivingEntityRenderPre(RenderLivingEvent.Pre event) {
        GlStateManager.pushMatrix();

        IAttributeInstance scaleAttribute = event.getEntity().getAttributeMap().getAttributeInstance(Potionss.scaleAttribute);
        if(scaleAttribute == null || scaleAttribute.getAttributeValue() == 1.0) {
            return;
        }

        double scale = scaleAttribute.getAttributeValue();
        GlStateManager.translate(-event.getX() * scale + event.getX(), -event.getY() * scale + event.getY(), -event.getZ() * scale + event.getZ());
        GlStateManager.scale(scale, scale, scale);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onLivingEntityRenderPost(RenderLivingEvent.Post event) {
        GlStateManager.popMatrix();
    }



    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        IAttributeInstance scaleAttribute = event.player.getAttributeMap().getAttributeInstance(Potionss.scaleAttribute);
        if(scaleAttribute == null) {
            return;
        }

        double scale = scaleAttribute.getAttributeValue();
        MiniaturizationPotion.setEntitySize(event.player, (float)scale);
    }



    /*
     * If an entity has been shrunk, scale and limit its upward motion when jumping
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        IAttributeInstance scaleAttribute = entity.getAttributeMap().getAttributeInstance(Potionss.scaleAttribute);
        if(scaleAttribute == null || scaleAttribute.getAttributeValue() == 1.0) {
            return;
        }

        double scale = scaleAttribute.getAttributeValue();
        if(entity.motionY >= 0) {
            entity.motionY *= MathHelper.clamp_double(scale*2d, 0.50f, 1.0f);
        }
    }

    /*
     * We are kinda injecting a new attribute type to all living entities when they
     * are instantiated. We need this because minecraft needs to know about all attributes
     * an entity has once it loads its NBT data or it throws their values away on load.
     * So we need to register our custom attributes very early in an entities lifecycle,
     * this is as early as it gets.
     */
    @SubscribeEvent
    public static void onLivingSpawn(EntityEvent.EntityConstructing event) {
        if(!(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase entity = (EntityLivingBase) event.getEntity();
        entity.getAttributeMap().registerAttribute(Potionss.scaleAttribute);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        IAttributeInstance scaleAttribute = entity.getAttributeMap().getAttributeInstance(Potionss.scaleAttribute);
        if(scaleAttribute == null || scaleAttribute.getAttributeValue() == 1.0) {
            return;
        }

        double baseChance = 0.1f;
        double scale = scaleAttribute.getAttributeValue();

        baseChance *= 1 / scale;
        if(event.getLootingLevel() > 0) {
            baseChance *= (event.getLootingLevel()/2.0)+1.0;
        }

        if(Math.random() > baseChance) {
            return;
        }

        ItemStack miniFluidDropDrop = new ItemStack(Itemss.miniFluidDrop);
        EntityItem entityItem = new EntityItem(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, miniFluidDropDrop);
        event.getDrops().add(entityItem);
    }
}
