package dev.compactmods.machines.datagen.basic_room_templates.lang;

import dev.compactmods.machines.datagen.base.lang.BaseLangGenerator;
import net.minecraft.data.PackOutput;

public class RoomTemplatesFrenchLangGenerator extends BaseLangGenerator {
    public RoomTemplatesFrenchLangGenerator(PackOutput packOutput) {
        super(packOutput, "fr_fr");
    }

    @Override
    protected String getMachineTranslation() {
        return "Compact Machine";
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();
        blocksAndItems();
    }

    private void blocksAndItems() {
        final var machineTranslation = getMachineTranslation();
        add("machine.compactmachines.tiny", "%s (%s)".formatted(machineTranslation, "Minuscule"));
        add("machine.compactmachines.small", "%s (%s)".formatted(machineTranslation, "Petite"));
        add("machine.compactmachines.normal", "%s (%s)".formatted(machineTranslation, "Normale"));
        add("machine.compactmachines.large", "%s (%s)".formatted(machineTranslation, "Grande"));
        add("machine.compactmachines.giant", "%s (%s)".formatted(machineTranslation, "Géante"));
        add("machine.compactmachines.colossal", "%s (%s)".formatted(machineTranslation, "Colossale"));
    }
}
