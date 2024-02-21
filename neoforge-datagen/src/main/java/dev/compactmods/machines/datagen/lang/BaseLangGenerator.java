package dev.compactmods.machines.datagen.lang;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.advancement.Advancements;
import dev.compactmods.machines.datagen.util.AdvancementLangBuilder;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.client.creative.CreativeTabs;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static org.apache.commons.lang3.StringUtils.capitalize;

public abstract class BaseLangGenerator extends LanguageProvider {

    private final String locale;

    public BaseLangGenerator(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), Constants.MOD_ID, locale);
        this.locale = locale;
    }

    @SuppressWarnings("unused")
    protected String getDirectionTranslation(Direction dir) {
        return capitalize(dir.getSerializedName());
    }

    protected String getMachineTranslation() {
        return "Compact Machine";
    }

    @Override
    protected void addTranslations() {
        // Machine Block names
        final var machineTranslation = getMachineTranslation();

        // Direction Names
        for (var dir : Direction.values()) {
            add(Constants.MOD_ID + ".direction." + dir.getSerializedName(), getDirectionTranslation(dir));
        }
    }

    protected void addTooltip(ResourceLocation id, String translation) {
        add(TranslationUtil.tooltipId(id), translation);
    }

    protected void addCreativeTab(ResourceLocation id, String translation) {
        add(Util.makeDescriptionId("itemGroup", id), translation);
    }

    protected void addAdvancementTranslations() {
        advancement(Advancements.FOUNDATIONS)
                .title("Foundations")
                .description("Obtain a breakable wall block.");

        advancement(Advancements.GOT_SHRINKING_DEVICE)
                .title("Personal Shrinking Device")
                .description("Obtain a Personal Shrinking Device");

        advancement(Advancements.HOW_DID_YOU_GET_HERE)
                .title("How Did You Get Here?!")
                .description("Which machine is the player in?!");

        advancement(Advancements.ROOT)
                .title("Compact Machines")
                .noDesc();

        advancement(Advancements.RECURSIVE_ROOMS)
                .title("Recursive Rooms")
                .description("To understand recursion, you must first understand recursion.");
    }

    protected AdvancementLangBuilder advancement(ResourceLocation advancement) {
        return new AdvancementLangBuilder(this, advancement);
    }

    protected void addCommand(ResourceLocation id, String translation) {
        this.add(TranslationUtil.commandId(id), translation);
    }

    protected void addMessage(ResourceLocation id, String translation) {
        this.add(TranslationUtil.messageId(id), translation);
    }
}
