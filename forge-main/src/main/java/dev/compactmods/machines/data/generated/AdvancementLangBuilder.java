package dev.compactmods.machines.data.generated;

import dev.compactmods.machines.data.generated.lang.BaseLangGenerator;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.resources.ResourceLocation;

public class AdvancementLangBuilder {


    private final BaseLangGenerator provider;
    private final ResourceLocation advancement;

    public AdvancementLangBuilder(BaseLangGenerator add, ResourceLocation adv) {
        this.provider = add;
        this.advancement = adv;
    }

    public AdvancementLangBuilder title(String title) {
        provider.add(TranslationUtil.advId(advancement), title);
        return this;
    }

    public AdvancementLangBuilder description(String description) {
        provider.add(TranslationUtil.advId(advancement) + ".desc", description);
        return this;
    }

    public AdvancementLangBuilder noDesc() {
        return description("");
    }
}
