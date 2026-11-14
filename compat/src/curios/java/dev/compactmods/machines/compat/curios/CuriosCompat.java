//package dev.compactmods.machines.compat.curios;
//
//import dev.compactmods.machines.core.CompactMachinesCore;
//import dev.compactmods.machines.api.shrinking.PSDTags;
//import dev.compactmods.machines.shrinking.Shrinking;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.entity.LivingEntity;
//import top.theillusivec4.curios.api.CuriosApi;
//
//import javax.annotation.Nonnull;
//
//public class CuriosCompat {
//
//    public static final Identifier PSD_VALIDATOR = CompactMachinesCore.modRL("has_shrinking_config");
//
//    public static void register() {
//
//        CuriosApi.registerCurioPredicate(PSD_VALIDATOR, result -> {
//            final var stack = result.stack();
//            return stack.is(PSDTags.ITEM) || stack.has(Shrinking.DataComponents.SHRINKING_CONFIG);
//        });
//    }
//
//    public static boolean hasPsdCurio(@Nonnull LivingEntity ent) {
//        final var inv = CuriosApi.getCuriosInventory(ent)
//                .flatMap(handler -> handler.getStacksHandler("psd"))
//                .orElse(null);
//
//        if(inv == null) return false;
//
//        for(var slot = 0; slot < inv.getSlots(); slot++) {
//            var slotItem = inv.getStacks().getStackInSlot(slot);
//            if(slotItem.isEmpty()) continue;
//
//            if(slotItem.has(Shrinking.DataComponents.SHRINKING_CONFIG) || slotItem.is(PSDTags.ITEM))
//                return true;
//        }
//
//        return false;
//    }
//}
