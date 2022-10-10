package dev.compactmods.machines.util;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.IOException;
import java.util.function.Consumer;

public class SavedDataHelper {

    public static void processFile(DimensionDataStorage storage, String dataName, Consumer<CompoundTag> parser) throws IOException {
        CompoundTag compoundtag = storage.readTagFromDisk(dataName, SharedConstants.getCurrentVersion().getWorldVersion());
        parser.accept(compoundtag.getCompound("data"));
    }

    public static <T extends SavedData> void saveFile(DimensionDataStorage storage, String dataName, T instance) {
        storage.set(dataName, instance);
        storage.save();
    }
}
