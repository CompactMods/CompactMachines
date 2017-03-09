package org.dave.cm2.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.init.Potionss;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.misc.CreativeTabCM2;

public class ItemMiniFluidDrop extends ItemFood {
    public ItemMiniFluidDrop() {
        super(1, 0.1F, false);

        //this.setPotionEffect(new PotionEffect(Potionss.miniaturizationPotion, 400, 1, false, false), 0.8f);
        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
    }

    @Override
    public Item setUnlocalizedName(String name) {
        if(!name.startsWith(CompactMachines2.MODID + ".")) {
            name = CompactMachines2.MODID + "." + name;
        }
        return super.setUnlocalizedName(name);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        super.onFoodEaten(stack, worldIn, player);

        int duration = ConfigurationHandler.PotionSettings.onEatDuration;
        int amplifier = ConfigurationHandler.PotionSettings.onEatAmplifier;

        if(duration > 0) {
            PotionEffect active = player.getActivePotionEffect(Potionss.miniaturizationPotion);

            PotionEffect effect = new PotionEffect(Potionss.miniaturizationPotion, duration, amplifier, false, false);
            if (active != null) {
                active.combine(effect);
            } else {
                player.addPotionEffect(effect);
            }
        }

    }
}
