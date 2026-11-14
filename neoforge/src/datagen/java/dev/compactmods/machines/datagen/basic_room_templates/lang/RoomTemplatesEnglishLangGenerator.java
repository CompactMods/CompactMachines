package dev.compactmods.machines.datagen.basic_room_templates.lang;

import dev.compactmods.machines.datagen.base.lang.BaseLangGenerator;
import net.minecraft.data.PackOutput;

public class RoomTemplatesEnglishLangGenerator extends BaseLangGenerator {
    public RoomTemplatesEnglishLangGenerator(PackOutput packOutput) {
        super(packOutput, "en_us");
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();
        blocksAndItems();
    }

    private void blocksAndItems() {
        final var machineTranslation = getMachineTranslation();
        add("machine.CompactMachinesCore.tiny", "%s (%s)".formatted(machineTranslation, "Tiny"));
        add("machine.CompactMachinesCore.small", "%s (%s)".formatted(machineTranslation, "Small"));
        add("machine.CompactMachinesCore.normal", "%s (%s)".formatted(machineTranslation, "Normal"));
        add("machine.CompactMachinesCore.large", "%s (%s)".formatted(machineTranslation, "Large"));
        add("machine.CompactMachinesCore.giant", "%s (%s)".formatted(machineTranslation, "Giant"));
        add("machine.CompactMachinesCore.colossal", "%s (%s)".formatted(machineTranslation, "Colossal"));
    }
}
