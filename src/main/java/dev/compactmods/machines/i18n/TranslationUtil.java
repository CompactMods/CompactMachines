package dev.compactmods.machines.i18n;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public abstract class TranslationUtil {

    public static String messageId(ResourceLocation message) {
        return Util.makeDescriptionId("message", message);
    }

    public static MutableComponent message(ResourceLocation message) {
        return Component.translatable(messageId(message));
    }

    public static MutableComponent message(ResourceLocation message, Object... params) {
        return Component.translatable(messageId(message), params);
    }

    public static String tooltipId(ResourceLocation tooltip) {
        return Util.makeDescriptionId("tooltip", tooltip);
    }

    public static MutableComponent tooltip(ResourceLocation tooltip) {
        return Component.translatable(tooltipId(tooltip));
    }

    public static MutableComponent tooltip(ResourceLocation tooltip, Object... params) {
        return Component.translatable(tooltipId(tooltip), params);
    }

    public static String advId(ResourceLocation tooltip) {
        return Util.makeDescriptionId("advancement", tooltip);
    }

    public static MutableComponent advancement(ResourceLocation advancement) {
        return Component.translatable(advId(advancement));
    }

    public static MutableComponent advancementTitle(ResourceLocation advancement) {
        return advancement(advancement);
    }

    public static MutableComponent advancementDesc(ResourceLocation advancement) {
        return Component.translatable(Util.makeDescriptionId("advancement", advancement) + ".desc");
    }

    public static MutableComponent jeiInfo(ResourceLocation jei) {
        return Component.translatable(Util.makeDescriptionId("jei", jei));
    }

    public static String commandId(ResourceLocation s) {
        return Util.makeDescriptionId("command", s);
    }

    public static MutableComponent command(ResourceLocation s) {
        return Component.translatable(commandId(s));
    }

    public static MutableComponent command(ResourceLocation s, Object... params) {
        return Component.translatable(commandId(s), params);
    }

    public static String tunnelId(ResourceLocation id) {
        return "item." + id.getNamespace() + ".tunnels." + id.getPath().replace('/', '.');
    }
}
