//package dev.compactmods.machines.compat.jei;
//
//import dev.compactmods.machines.CompactMachinesCommon;
//import dev.compactmods.machines.core.CompactMachinesCore;
//import dev.compactmods.machines.CMDataComponents;
//import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
//import dev.compactmods.machines.machine.Machines;
//import dev.compactmods.machines.shrinking.Shrinking;
//import mezz.jei.api.IModPlugin;
//import mezz.jei.api.JeiPlugin;
//import mezz.jei.api.constants.VanillaTypes;
//import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
//import mezz.jei.api.ingredients.subtypes.UidContext;
//import mezz.jei.api.registration.IRecipeRegistration;
//import mezz.jei.api.registration.ISubtypeRegistration;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.neoforge.server.ServerLifecycleHooks;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import javax.annotation.ParametersAreNonnullByDefault;
//
//@JeiPlugin
//public class CompactMachinesJeiPlugin implements IModPlugin {
//    @Override
//    public @NotNull Identifier getPluginUid() {
//        return CompactMachinesCore.identifier("main");
//    }
//
//    @Override
//    public void registerRecipes(IRecipeRegistration registration) {
//        registration.addIngredientInfo(
//                Machines.Items.unboundColored(CompactMachinesCommon.BRAND_MACHINE_COLOR),
//                VanillaTypes.ITEM_STACK,
//                Component.translatable("jei.CompactMachinesCore.machines"));
//
//        // Add all known template JEI infos
//        RoomTemplateHelper.getTemplateHolders(ServerLifecycleHooks.getCurrentServer().registryAccess())
//                .map(Machines.Items::forNewRoom)
//                .forEach(t -> registration.addIngredientInfo(t, VanillaTypes.ITEM_STACK,
//                        Component.translatable("jei.CompactMachinesCore.machines")));
//
//        registration.addIngredientInfo(
//                new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()),
//                VanillaTypes.ITEM_STACK,
//                Component.translatable("jei.CompactMachinesCore.shrinking_device"));
//    }
//
//    @Override
//    @ParametersAreNonnullByDefault
//    public void registerItemSubtypes(ISubtypeRegistration registration) {
//        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Machines.Items.UNBOUND_MACHINE.get(),
//                (ingredient, context) -> ingredient.get(CMDataComponents.ROOM_TEMPLATE_ID));
//    }
//}
