package dev.compactmods.machines.datagen.base;


import dev.compactmods.machines.machine.client.MachineColors;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.datagen.models.CMBlockModelGenerators;
import dev.compactmods.machines.datagen.models.CMItemModelGenerators;
import dev.compactmods.machines.datagen.models.CMModelProvider;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class ModelAndStateGenerator extends CMModelProvider {

    public ModelAndStateGenerator(PackOutput output) {
        super(output);
    }

    protected void registerModels(CMBlockModelGenerators blockModels, CMItemModelGenerators itemModels) {

        blockModels.createTrivialBlock(Rooms.Blocks.SOLID_WALL.get(), TexturedModel.CUBE);
        blockModels.createTrivialBlock(Rooms.Blocks.BREAKABLE_WALL.get(), TexturedModel.CUBE);

        blockModels.createAirLikeBlock(Dimension.BLOCK_MACHINE_VOID_AIR.get(),
                new Material(CompactMachinesCore.identifier("none")));

        blockModels.createCompactMachine(Machines.Blocks.MACHINE);

        blockModels.registerSimpleTintedItemModel(Machines.Blocks.MACHINE.get(), CompactMachinesCore.identifier("block/machine"),
                new MachineColors.MachineColorComponentItemTintSource());


        blockModels.createSimpleWithTextures(Villagers.SPATIAL_WORKBENCH, ModelTemplates.CUBE_TOP, new TextureMapping()
                .put(TextureSlot.PARTICLE, new Material(modLocation("block/workbench/side")))
                .put(TextureSlot.TOP, new Material(modLocation("block/workbench/top")))
                .put(TextureSlot.SIDE, new Material(modLocation("block/workbench/side"))));

        itemModels.generateFlatItem(Shrinking.Items.PERSONAL_SHRINKING_DEVICE.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModels.generateFlatItem(Shrinking.Items.SHRINKING_MODULE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Shrinking.Items.ENLARGING_MODULE.asItem(), ModelTemplates.FLAT_ITEM);

        blockModels.createTunnelWall(Tunnels.Blocks.TUNNEL_WALL);
        itemModels.generateFlatItem(Tunnels.Items.TUNNEL.asItem(), ModelTemplates.FLAT_ITEM);
    }

}
