package org.dave.cm2.miniaturization;

import com.google.common.collect.Lists;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.dave.cm2.init.Potionss;
import org.dave.cm2.utility.Logz;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;


public class MiniaturizationPotion extends Potion {
    public static final AttributeModifier[] SCALE_MODIFIER = {
            new AttributeModifier("Scale 75%", -0.25, 0),
            new AttributeModifier("Scale 50%", -0.50, 0),
            new AttributeModifier("Scale 25%", -0.75, 0),
            new AttributeModifier("Scale 12.5%", -0.875, 0)
    };

    public MiniaturizationPotion(boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);

        this.setPotionName("effect.shrink");
        this.setIconIndex(3, 2);
        this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "838296b1-ad82-438b-ba64-89633b5472f0", -0.15, 2);
    }

    @Override
    public boolean isInstant() {
        return true;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMap, int amplifier) {
        super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMap, amplifier);

        if(amplifier < 0 || amplifier > 3) {
            return;
        }

        IAttributeInstance scaleAttribute = attributeMap.getAttributeInstance(Potionss.scaleAttribute);
        if(scaleAttribute != null) {
            // These should not stack -> remove all previous modifiers
            Collection<AttributeModifier> collection = scaleAttribute.getModifiers();

            if (collection != null) {
                Lists.newArrayList(collection).forEach(scaleAttribute::removeModifier);
            }

            scaleAttribute.applyModifier(SCALE_MODIFIER[amplifier]);
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMap, int amplifier) {
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMap, amplifier);

        setEntitySize(entityLivingBaseIn, 1.0f);

        if(amplifier < 0 || amplifier > 3) {
            return;
        }

        IAttributeInstance scaleAttribute = attributeMap.getAttributeInstance(Potionss.scaleAttribute);
        if(scaleAttribute != null) {
            scaleAttribute.removeModifier(SCALE_MODIFIER[amplifier]);
        }
    }

    private static class BaseSize {
        float width;
        float height;

        public BaseSize(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }

    private static BaseSize getBaseValues(EntityLivingBase entity) {
        if(entity instanceof EntityPlayer) {
            return new BaseSize(0.6f, 1.8f);
        }

        try {
            Constructor<?> c = entity.getClass().getDeclaredConstructor(World.class);
            c.setAccessible(true);
            EntityLivingBase copy = (EntityLivingBase) c.newInstance(new Object[] { entity.getEntityWorld() });

            return new BaseSize(copy.width, copy.height);
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }

        return new BaseSize(0.6f, 1.8f);
    }

    public static void setEntitySize(EntityLivingBase entity, float scale) {
        BaseSize baseSize = getBaseValues(entity);
        float baseWidth = baseSize.width;
        float baseHeight = baseSize.height;

        float newWidth = baseWidth * scale;
        float newHeight = baseHeight * scale;


        AxisAlignedBB originalBB = entity.getEntityBoundingBox();
        entity.width = newWidth;
        entity.height = newHeight;

        AxisAlignedBB newBB = new AxisAlignedBB(
            entity.posX - newWidth / 2, originalBB.minY,             entity.posZ - newWidth / 2,
            entity.posX + newWidth / 2, originalBB.minY + newHeight, entity.posZ + newWidth / 2
        );

        entity.setEntityBoundingBox(newBB);

        if(entity instanceof EntityPlayer) {
            ((EntityPlayer)entity).eyeHeight = ((EntityPlayer)entity).getDefaultEyeHeight() * scale;
        }
    }

    public static void applyPotion(EntityLivingBase entity, int duration, int amplifier) {
        if(duration <= 0) {
            return;
        }

        PotionEffect active = entity.getActivePotionEffect(Potionss.miniaturizationPotion);
        PotionEffect effect = new PotionEffect(Potionss.miniaturizationPotion, duration, amplifier, false, false);
        if (active != null) {
            active.combine(effect);
        } else {
            entity.addPotionEffect(effect);
        }

    }
}
