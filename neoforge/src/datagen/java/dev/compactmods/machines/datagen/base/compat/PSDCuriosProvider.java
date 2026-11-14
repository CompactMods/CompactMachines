//package dev.compactmods.machines.datagen.base.compat;
//
//
//import net.minecraft.core.HolderLookup;
//import net.minecraft.data.PackOutput;
//import top.theillusivec4.curios.api.CuriosDataProvider;
//
//import java.util.concurrent.CompletableFuture;
//
//public class PSDCuriosProvider extends CuriosDataProvider {
//
//    public PSDCuriosProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
//        super(CompactMachinesCore.MOD_ID, output, existingFileHelper, lookupProvider);
//    }
//
//    @Override
//    public void generate(HolderLookup.Provider registries) {
//        this.createSlot("psd")
//                .icon(CompactMachinesCore.modRL("slot/empty_psd"))
//                .size(1)
//                .addValidator(CuriosCompat.PSD_VALIDATOR);
//
//        this.createEntities("psd")
//                .addPlayer()
//                .addSlots("psd");
//    }
//}
