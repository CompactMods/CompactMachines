package dev.compactmods.machines.datagen.base.lang;


import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class BaseLangGenerator extends LanguageProvider {

    public BaseLangGenerator(PackOutput packOutput, String locale) {
        super(packOutput, CompactMachinesCore.MOD_ID, locale);
    }

    protected String getMachineTranslation() {
        return "Compact Machine";
    }

    @Override
    protected void addTranslations() {}

    protected void addVillagerProfession(Holder<VillagerProfession> profession, String name) {
        final var rl = profession.getKey().identifier();
        add("entity." + rl.getNamespace() + ".villager." + rl.getPath(), name);
    }

    protected void addGamerule(Identifier key, String title, String description) {
        add(key.toLanguageKey("gamerule"), title);
        add(key.toLanguageKey("gamerule", "description"), description);
    }

    protected void addCreativeTab(Identifier id, String translation) {
        add(Util.makeDescriptionId("itemGroup", id), translation);
    }

    protected void advancement(Identifier adv, String title, String desc) {
        add(Util.makeDescriptionId("advancement", adv), title);
        add(Util.makeDescriptionId("advancement", adv) + ".desc", desc != null ? desc : "");
    }
}
