package dev.compactmods.machines.datagen;

import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public class PackMetaGenerator extends PackMetadataGenerator {
    public PackMetaGenerator(PackOutput packOut) {
        super(packOut);

        add(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal("CompactMachines resources"),
                DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA))
        );
    }
}
