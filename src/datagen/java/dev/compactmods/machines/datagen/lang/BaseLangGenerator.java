package dev.compactmods.machines.datagen.lang;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.datagen.AdvancementLangBuilder;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.room.RoomSize;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.capitalize;

public abstract class BaseLangGenerator extends LanguageProvider {

    private final String locale;

    public BaseLangGenerator(DataGenerator gen, String locale) {
        super(gen, CompactMachines.MOD_ID, locale);
        this.locale = locale;
    }

    protected abstract String getSizeTranslation(RoomSize size);

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
        for(var size : RoomSize.values()) {
            add(CompactMachineBlock.getBySize(size), String.format("%s (%s)", getMachineTranslation(), getSizeTranslation(size)));
        }

        // Direction Names
        for (var dir : Direction.values()) {
            add(CompactMachines.MOD_ID + ".direction." + dir.getSerializedName(), getDirectionTranslation(dir));
        }
    }

    protected void addTooltip(ResourceLocation id, String translation) {
        add(TranslationUtil.tooltipId(id), translation);
    }

    protected void addTunnel(Supplier<TunnelDefinition> tunnel, String name) {
        add(TranslationUtil.tunnelId(tunnel.get().getRegistryName()), name);
    }

    void addUpgradeItem(Supplier<RoomUpgrade> upgrade, String translation) {
        final var u = upgrade.get();
        if(u != null)
            add(u.getTranslationKey(), translation);
    }

    protected void addAdvancementTranslations() {
        advancement(Advancements.FOUNDATIONS)
                .title("Foundations")
                .description("Obtain a breakable wall block.");

        advancement(Advancements.CLAIMED_GIANT_MACHINE)
                .title("Got Enough Space?")
                .description("Claim a giant compact machine.");

        advancement(Advancements.CLAIMED_LARGE_MACHINE)
                .title("Room to Grow")
                .description("Claim a large compact machine.");

        advancement(Advancements.CLAIMED_MAX_MACHINE)
                .title("Room for Activities!")
                .description("Claim a maximum compact machine.");

        advancement(Advancements.CLAIMED_NORMAL_MACHINE)
                .title("Bigger on the Inside")
                .description("Claim a normal compact machine.");

        advancement(Advancements.CLAIMED_SMALL_MACHINE)
                .title("I Can Breathe")
                .description("Claim a small compact machine.");

        advancement(Advancements.CLAIMED_TINY_MACHINE)
                .title("Small Spaces, Big Ideas")
                .description("Claim a tiny compact machine.");

        advancement(Advancements.GOT_SHRINKING_DEVICE)
                .title("Personal Shrinking Device")
                .description("Obtain a Personal Shrinking Device");

        advancement(Advancements.HOW_DID_YOU_GET_HERE)
                .title("How Did You Get Here?!")
                .description("Which machine is the player in?!");

        advancement(Advancements.ROOT).title("Compact Machines").noDesc();

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
