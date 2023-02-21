package dev.compactmods.machines.data.generated.lang;

import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.data.generated.AdvancementLangBuilder;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.capitalize;

public abstract class BaseLangGenerator extends LanguageProvider {

    private final String locale;

    private final IForgeRegistry<RoomUpgrade> ACTIONS_REG;

    public BaseLangGenerator(DataGenerator gen, String locale) {
        super(gen, Constants.MOD_ID, locale);
        this.locale = locale;
        ACTIONS_REG = MachineRoomUpgrades.REGISTRY.get();
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
    @SuppressWarnings("removal")
    protected void addTranslations() {
        // Machine Block names
        final var machineTranslation = getMachineTranslation();
        for(var size : RoomSize.values()) {
            add(LegacySizedCompactMachineBlock.getBySize(size), "%s (%s)".formatted(machineTranslation, getSizeTranslation(size)));
        }

        // Direction Names
        for (var dir : Direction.values()) {
            add(Constants.MOD_ID + ".direction." + dir.getSerializedName(), getDirectionTranslation(dir));
        }
    }

    protected void addTooltip(ResourceLocation id, String translation) {
        add(TranslationUtil.tooltipId(id), translation);
    }

    protected void addTunnel(Supplier<TunnelDefinition> tunnel, String name) {
        add(TranslationUtil.tunnelId(Tunnels.getRegistryId(tunnel.get())), name);
    }

    void add(Supplier<RoomUpgrade> upgrade, String translation) {
        final var u = upgrade.get();
        final var id = ACTIONS_REG.getKey(u);
        if(u != null)
            add(Util.makeDescriptionId("upgrade.action", id), translation);
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
