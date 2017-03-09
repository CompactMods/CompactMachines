package org.dave.cm2.miniaturization;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import org.dave.cm2.init.Potionss;


public class MiniaturizationPotion extends Potion {
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
            scaleAttribute.removeModifier(SCALE_MODIFIER[0]);
            scaleAttribute.removeModifier(SCALE_MODIFIER[1]);
            scaleAttribute.removeModifier(SCALE_MODIFIER[2]);
            scaleAttribute.removeModifier(SCALE_MODIFIER[3]);

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

        // TODO: Without this players might get stuck in a shrunken state, what's up with that?
        // scaleAttribute.removeAllModifiers();
    }

    public static final AttributeModifier[] SCALE_MODIFIER = {
            new AttributeModifier("Scale 75%", -0.25, 0),
            new AttributeModifier("Scale 50%", -0.50, 0),
            new AttributeModifier("Scale 25%", -0.75, 0),
            new AttributeModifier("Scale 12.5%", -0.875, 0)
    };

    public static void setEntitySize(EntityLivingBase entity, float scale) {
        float baseWidth = 0.6F;
        float baseHeight = 1.8F;

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
}
