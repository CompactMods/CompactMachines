package dev.compactmods.machines.forge.compat.curios;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.shrinking.PSDTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.Nonnull;

public class CuriosCompat {

    private static final ResourceLocation CURIO_TEXTURE = new ResourceLocation(Constants.MOD_ID, "curios/empty_psd");


    public static void sendIMC() {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("psd")
                .size(1)
                .icon(CURIO_TEXTURE)
                .build());
    }

    private static boolean isPsd(ItemStack stack) {
        return stack.is(PSDTags.ITEM);
    }

    public static void addTextures(final TextureStitchEvent.Pre stitch) {
        stitch.addSprite(CURIO_TEXTURE);
    }

    public static boolean hasPsdCurio(@Nonnull LivingEntity ent) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(ent, CuriosCompat::isPsd)
                .isPresent();
    }
}
