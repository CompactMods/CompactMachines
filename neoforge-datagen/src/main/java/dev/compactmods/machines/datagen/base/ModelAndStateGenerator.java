package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.machine.MachineColors;
import dev.compactmods.machines.datagen.models.CMBlockModelGenerators;
import dev.compactmods.machines.datagen.models.CMItemModelGenerators;
import dev.compactmods.machines.datagen.models.CMModelProvider;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;

public class ModelAndStateGenerator extends CMModelProvider {

    public ModelAndStateGenerator(PackOutput output) {
        super(output);
    }

    protected void registerModels(CMBlockModelGenerators blockModels, CMItemModelGenerators itemModels) {

        blockModels.createTrivialBlock(Rooms.Blocks.SOLID_WALL.get(), TexturedModel.CUBE);
        blockModels.createTrivialBlock(Rooms.Blocks.BREAKABLE_WALL.get(), TexturedModel.CUBE);

        blockModels.createAirLikeBlock(Dimension.BLOCK_MACHINE_VOID_AIR.get(),
                CompactMachines.modRL("none"));

        blockModels.createCompactMachine(Machines.Blocks.BOUND_MACHINE);
        blockModels.createCompactMachine(Machines.Blocks.UNBOUND_MACHINE);

        blockModels.registerSimpleTintedItemModel(Machines.Blocks.UNBOUND_MACHINE.get(), CompactMachines.modRL("block/machine"),
                new MachineColors.MachineColorComponentItemTintSource());

        blockModels.registerSimpleTintedItemModel(Machines.Blocks.BOUND_MACHINE.get(), CompactMachines.modRL("block/machine"),
                new MachineColors.MachineColorComponentItemTintSource());


        blockModels.createSimpleWithTextures(Villagers.SPATIAL_WORKBENCH, ModelTemplates.CUBE_TOP, new TextureMapping()
                .put(TextureSlot.PARTICLE, modLocation("block/workbench/side"))
                .put(TextureSlot.TOP, modLocation("block/workbench/top"))
                .put(TextureSlot.SIDE, modLocation("block/workbench/side")));

        itemModels.generateFlatItem(Shrinking.PERSONAL_SHRINKING_DEVICE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModels.generateFlatItem(Shrinking.SHRINKING_MODULE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Shrinking.ENLARGING_MODULE.asItem(), ModelTemplates.FLAT_ITEM);

        itemModels.generateRoomCoreItem(Rooms.Items.ROOM_CORE);
    }

}
