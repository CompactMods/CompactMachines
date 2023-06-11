package dev.compactmods.machines.forge.compat.jei;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.JeiInfo;
import dev.compactmods.machines.api.room.Rooms;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.machine.item.LegacyCompactMachineItem;
import dev.compactmods.machines.forge.machine.item.MachineItemUtil;
import dev.compactmods.machines.forge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.forge.shrinking.Shrinking;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.i18n.TranslationUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.stream.Collectors;

@JeiPlugin
public class CompactMachinesJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Constants.MOD_ID, "main");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        final var ingManager = registration.getIngredientManager();

        registration.addIngredientInfo(
                UnboundCompactMachineItem.unbound(),
                VanillaTypes.ITEM_STACK,
                TranslationUtil.jeiInfo(JeiInfo.MACHINE));

        // Add all known template JEI infos
        Rooms.getTemplates(ServerLifecycleHooks.getCurrentServer())
                .entrySet()
                .stream()
                .map(t -> UnboundCompactMachineItem.forTemplate(t.getKey().location(), t.getValue()))
                .forEach(t -> registration.addIngredientInfo(t, VanillaTypes.ITEM_STACK, TranslationUtil.jeiInfo(JeiInfo.MACHINE)));

        registration.addIngredientInfo(
                new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()),
                VanillaTypes.ITEM_STACK,
                TranslationUtil.jeiInfo(JeiInfo.SHRINKING_DEVICE));

        //noinspection removal Will be removing once 5.3 or 6.0 drops
        final var allLegacyMachines = ForgeRegistries.ITEMS.tags()
                .getTag(LegacyCompactMachineItem.TAG)
                .stream()
                .map(ItemStack::new)
                .collect(Collectors.toSet());

        ingManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, allLegacyMachines);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(Tunnels.ITEM_TUNNEL.get());
        registration.registerSubtypeInterpreter(Machines.UNBOUND_MACHINE_BLOCK_ITEM.get(),
                (ingredient, context) -> MachineItemUtil.getTemplateId(ingredient).toString());
    }
}
