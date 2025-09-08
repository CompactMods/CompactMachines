package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.WallConstants;
import dev.compactmods.machines.client.machine.MachineColors;
import dev.compactmods.machines.datagen.models.CMModelProvider;
import dev.compactmods.machines.datagen.models.CompactMachineModelTemplate;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModelAndStateGenerator extends CMModelProvider {

    public ModelAndStateGenerator(PackOutput output) {
        super(output);
    }

    protected void registerModels(CMBlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        blockModels.createTrivialBlock(Rooms.Blocks.SOLID_WALL.get(), TexturedModel.CUBE);
        blockModels.createTrivialBlock(Rooms.Blocks.BREAKABLE_WALL.get(), TexturedModel.CUBE);

        blockModels.createAirLikeBlock(Dimension.BLOCK_MACHINE_VOID_AIR.get(),
                CompactMachines.modRL("none"));

        blockModels.createCompactMachine(Machines.Blocks.BOUND_MACHINE.get());
        blockModels.createCompactMachine(Machines.Blocks.UNBOUND_MACHINE.get());

        blockModels.registerSimpleTintedItemModel(Machines.Blocks.UNBOUND_MACHINE.get(), CompactMachines.modRL("block/machine"),
                new MachineColors.MachineColorComponentItemTintSource());

        blockModels.registerSimpleTintedItemModel(Machines.Blocks.BOUND_MACHINE.get(), CompactMachines.modRL("block/machine"),
                new MachineColors.MachineColorComponentItemTintSource());

        items(itemModels);

        blockModels.run();

//        this.simpleBlock(MachineRoomUpgrades.WORKBENCH_BLOCK.get(), models()
//                .cubeTop("block/workbench", modLoc("block/workbench/top"), modLoc("block/workbench/sides")));
    }

    private void items(ItemModelGenerators itemModels) {
        final var psd = ModelTemplates.FLAT_HANDHELD_ITEM.create(
                Shrinking.PERSONAL_SHRINKING_DEVICE.get(),
                TextureMapping.layer0(this.modLocation("item/personal_shrinking_device")),
                itemModels.modelOutput);


        final var shrinking = ModelTemplates.FLAT_ITEM.create(
                Shrinking.SHRINKING_MODULE.get(),
                TextureMapping.layer0(this.modLocation("item/atom_shrinker")),
                itemModels.modelOutput
        );

        final var enlarging = ModelTemplates.FLAT_ITEM.create(
                Shrinking.ENLARGING_MODULE.get(),
                TextureMapping.layer0(this.modLocation("item/atom_enlarger")),
                itemModels.modelOutput
        );

        itemModels.itemModelOutput.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), ItemModelUtils.plainModel(psd));
        itemModels.itemModelOutput.accept(Shrinking.SHRINKING_MODULE.get(), ItemModelUtils.plainModel(shrinking));
        itemModels.itemModelOutput.accept(Shrinking.ENLARGING_MODULE.get(), ItemModelUtils.plainModel(enlarging));
    }

    public static class CMBlockModelGenerators extends BlockModelGenerators {

        public CMBlockModelGenerators(Consumer<BlockModelDefinitionGenerator> p_387996_, ItemModelOutput p_387053_, BiConsumer<ResourceLocation, ModelInstance> p_387066_) {
            super(p_387996_, p_387053_, p_387066_);
        }

        @Override
        public void run() {
        }

        public void createCompactMachine(Block block) {
            final var id = CompactMachineModelTemplate.MACHINE_TEMPLATE.create(block,
                    new TextureMapping()
                            .put(TextureSlot.PARTICLE, CompactMachines.modRL("block/machine/tiny"))
                            .put(CompactMachineModelTemplate.BORDER_SLOT, CompactMachines.modRL("block/machine/border"))
                            .put(CompactMachineModelTemplate.OVERLAY_SLOT, CompactMachines.modRL("block/machine/overlay"))
                            .put(CompactMachineModelTemplate.TINT_SLOT, CompactMachines.modRL("block/machine/tint")),
                    this.modelOutput);

            // BlockModelDefinitionGenerator
            this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                    block,
                    BlockModelGenerators.plainVariant(id))
            );
        }
    }
}
