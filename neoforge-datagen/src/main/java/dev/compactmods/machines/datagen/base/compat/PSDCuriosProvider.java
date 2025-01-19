package dev.compactmods.machines.datagen.base.compat;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.compat.curios.CuriosCompat;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class PSDCuriosProvider extends CuriosDataProvider {

    public PSDCuriosProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(CompactMachines.MOD_ID, output, existingFileHelper, lookupProvider);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        this.createSlot("psd")
                .icon(CompactMachines.modRL("slot/empty_psd"))
                .size(1)
                .addValidator(CuriosCompat.PSD_VALIDATOR);

        this.createEntities("psd")
                .addPlayer()
                .addSlots("psd");
    }
}
