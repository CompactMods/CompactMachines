package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(Machines.BOUND_MACHINE_BLOCK_ITEM.getId().getPath(), modLoc("block/machine/machine"));
        withExistingParent(Machines.UNBOUND_MACHINE_BLOCK_ITEM.getId().getPath(), modLoc("block/machine/machine"));
        
        withExistingParent("solid_wall", modLoc("block/wall"));
        withExistingParent("wall", modLoc("block/wall"));

        basic(modLoc("personal_shrinking_device"))
                .texture("layer0", modLoc("item/personal_shrinking_device"));

        basic(Shrinking.SHRINKING_MODULE)
                .texture("layer0", modLoc("item/atom_shrinker"));

        basic(Shrinking.ENLARGING_MODULE)
                .texture("layer0", modLoc("item/atom_enlarger"));

//        basic(Shrinking.RESIZING_MODULE)
//                .texture("layer0", modLoc("item/atom_resizer"));
    }

    private ItemModelBuilder basic(ResourceLocation name) {
        return withExistingParent(name.getPath(), mcLoc("item/generated"));
    }

    private ItemModelBuilder basic(Supplier<Item> supplier) {
        Item i = supplier.get();
        return basic(BuiltInRegistries.ITEM.getKey(i));
    }
}
