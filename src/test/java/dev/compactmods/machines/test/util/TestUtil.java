package dev.compactmods.machines.test.util;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class TestUtil {

    public static void loadStructureIntoTestArea(GameTestHelper test, ResourceLocation structure, BlockPos relLocation) {
        final var structures = test.getLevel().getStructureManager();
        final var template = structures.get(structure);
        if(template.isEmpty())
            return;

        var placeAt = test.absolutePos(relLocation);
        template.get().placeInWorld(
                test.getLevel(),
                placeAt,
                placeAt,
                new StructurePlaceSettings(),
                test.getLevel().getRandom(),
                Block.UPDATE_ALL);
    }

}
