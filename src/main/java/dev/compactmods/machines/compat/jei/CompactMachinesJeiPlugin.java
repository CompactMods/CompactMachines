package dev.compactmods.machines.compat.jei;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.JeiInfo;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineItem;
import dev.compactmods.machines.api.room.RoomSize;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

@JeiPlugin
public class CompactMachinesJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CompactMachines.MOD_ID, "main");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Arrays.stream(RoomSize.values())
                .map(CompactMachineItem::getItemBySize)
                .forEach(i -> registration.addIngredientInfo(
                        new ItemStack(i),
                        VanillaTypes.ITEM_STACK,
                        TranslationUtil.jeiInfo(JeiInfo.MACHINE)));


        registration.addIngredientInfo(
                new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()),
                VanillaTypes.ITEM_STACK,
                TranslationUtil.jeiInfo(JeiInfo.SHRINKING_DEVICE));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(Tunnels.ITEM_TUNNEL.get());
    }
}
