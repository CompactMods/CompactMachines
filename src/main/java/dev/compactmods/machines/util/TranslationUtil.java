package dev.compactmods.machines.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class TranslationUtil {

    public static String messageId(ResourceLocation message) {
        return Util.makeDescriptionId("message", message);
    }

    public static TranslatableComponent message(ResourceLocation message) {
        return new TranslatableComponent(messageId(message));
    }

    public static TranslatableComponent message(ResourceLocation message, Object... params) {
        return new TranslatableComponent(messageId(message), params);
    }

    public static String tooltipId(ResourceLocation tooltip) {
        return Util.makeDescriptionId("tooltip", tooltip);
    }

    public static TranslatableComponent tooltip(ResourceLocation tooltip) {
        return new TranslatableComponent(tooltipId(tooltip));
    }

    public static TranslatableComponent tooltip(ResourceLocation tooltip, Object... params) {
        return new TranslatableComponent(tooltipId(tooltip), params);
    }

    public static String advId(ResourceLocation tooltip) {
        return Util.makeDescriptionId("advancement", tooltip);
    }

    public static TranslatableComponent advancement(ResourceLocation advancement) {
        return new TranslatableComponent(advId(advancement));
    }

    public static TranslatableComponent advancementTitle(ResourceLocation advancement) {
        return advancement(advancement);
    }

    public static TranslatableComponent advancementDesc(ResourceLocation advancement) {
        return new TranslatableComponent(Util.makeDescriptionId("advancement", advancement) + ".desc");
    }

    public static TranslatableComponent jeiInfo(ResourceLocation jei) {
        return new TranslatableComponent(Util.makeDescriptionId("jei", jei));
    }
}
