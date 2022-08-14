package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Constants.MOD_ID, existingFileHelper);
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

        withExistingParent(MachineRoomUpgrades.CHUNKLOADER.getId().toString(), mcLoc("item/generated"))
                .texture("layer0", modLoc("upgrades/chunkloader"));
    }

    private void machine(String size) {
        withExistingParent("machine_" + size, modLoc("block/machine/machine_" + size));
    }
}
