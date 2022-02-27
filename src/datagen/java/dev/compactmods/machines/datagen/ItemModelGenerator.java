package dev.compactmods.machines.datagen;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.room.RoomSize;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, CompactMachines.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var size : RoomSize.values())
            machine(size.getSerializedName());

        withExistingParent("solid_wall", modLoc("block/wall"));
        withExistingParent("wall", modLoc("block/wall"));

        withExistingParent("personal_shrinking_device", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/personal_shrinking_device"));

        withExistingParent("tunnel", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/tunnel"));
    }

    private void machine(String size) {
        withExistingParent("machine_" + size, modLoc("block/machine/machine_" + size));
    }
}
