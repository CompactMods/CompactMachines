package dev.compactmods.machines.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class TranslationUtil {

    public static String messageId(ResourceLocation message) {
        return Util.makeDescriptionId("message", message);
    }

    public static TranslationTextComponent message(ResourceLocation message) {
        return new TranslationTextComponent(messageId(message));
    }

    public static TranslationTextComponent message(ResourceLocation message, Object... params) {
        return new TranslationTextComponent(messageId(message), params);
    }

    public static String tooltipId(ResourceLocation tooltip) {
        return Util.makeDescriptionId("tooltip", tooltip);
    }

    public static TranslationTextComponent tooltip(ResourceLocation tooltip) {
        return new TranslationTextComponent(tooltipId(tooltip));
    }

    public static TranslationTextComponent tooltip(ResourceLocation tooltip, Object... params) {
        return new TranslationTextComponent(tooltipId(tooltip), params);
    }

    public static String advId(ResourceLocation tooltip) {
        return Util.makeDescriptionId("advancement", tooltip);
    }

    public static TranslationTextComponent advancement(ResourceLocation advancement) {
        return new TranslationTextComponent(advId(advancement));
    }

    public static TranslationTextComponent advancementTitle(ResourceLocation advancement) {
        return advancement(advancement);
    }

    public static TranslationTextComponent advancementDesc(ResourceLocation advancement) {
        return new TranslationTextComponent(Util.makeDescriptionId("advancement", advancement) + ".desc");
    }

    public static TranslationTextComponent jeiInfo(ResourceLocation jei) {
        return new TranslationTextComponent(Util.makeDescriptionId("jei", jei));
    }
}
