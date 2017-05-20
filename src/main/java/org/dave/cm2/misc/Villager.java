package org.dave.cm2.misc;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.init.Itemss;

import java.util.Random;

public class Villager {
    public static VillagerProfession shrinker;
    public static void init() {
        shrinker = new VillagerProfession("cm2:shrinker", "cm2:textures/entities/villager.png", "cm2:textures/entities/villager.png");
        GameRegistry.register(shrinker);

        VillagerCareer builder = new VillagerCareer(shrinker, "cm2.builder");

        // 10-20 blocks are worth 1 emeralds
        builder.addTrade(1, new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blockss.wallBreakable), new EntityVillager.PriceInfo(10,20)));
        builder.addTrade(1, new DirectTrade(new ItemStack(Items.EMERALD, 2), null, new ItemStack(Blockss.wallBreakable, 20)));

        // buy a psd for 18-30 emeralds
        builder.addTrade(1, new EntityVillager.ListItemForEmeralds(new ItemStack(Itemss.psd, 1, 0), new EntityVillager.PriceInfo(18,30)));

        VillagerCareer chemist = new VillagerCareer(shrinker, "cm2.chemist");

        // buy a bucket for 1 emeralds
        chemist.addTrade(1, new DirectTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.BUCKET), UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid)));
    }

    public static class DirectTrade implements EntityVillager.ITradeList {
        ItemStack inputOne;
        ItemStack inputTwo;
        ItemStack output;

        public DirectTrade(ItemStack inputOne, ItemStack inputTwo, ItemStack output) {
            this.inputOne = inputOne;
            this.inputTwo = inputTwo;
            this.output = output;
        }

        public void modifyMerchantRecipeList(MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(inputOne, inputTwo, output));
        }

        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(inputOne, inputTwo, output));
        }
    }
}
