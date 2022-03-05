package dev.compactmods.machines.compat.jei;

import java.util.Arrays;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.JeiInfo;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.i18n.TranslationUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class CompactMachinesJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CompactMachines.MOD_ID, "main");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Arrays.stream(RoomSize.values())
                .map(CompactMachineBlock::getItemBySize)
                .forEach(i -> registration.addIngredientInfo(
                        new ItemStack(i),
                        VanillaTypes.ITEM,
                        TranslationUtil.jeiInfo(JeiInfo.MACHINE)));


        registration.addIngredientInfo(
                new ItemStack(Registration.PERSONAL_SHRINKING_DEVICE.get()),
                VanillaTypes.ITEM,
                TranslationUtil.jeiInfo(JeiInfo.SHRINKING_DEVICE));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(Tunnels.ITEM_TUNNEL.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    }
}
