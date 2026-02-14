package dev.compactmods.machines.client.room;

import dev.compactmods.gander.level.VirtualLevel;
import dev.compactmods.gander.render.geometry.LevelBakery;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class ClientRoomPacketHandler {
    public static void handleBlockData(StructureTemplate blocks) {
        final var mc = Minecraft.getInstance();
        if(mc.screen instanceof MachineRoomScreen mrs) {

            var bounds = AABB.of(blocks.getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO));

            var virtualLevel = new VirtualLevel(Minecraft.getInstance().level.registryAccess(), true, level -> {
                level.refreshBlockEntityModels();

                var bakedLevel = LevelBakery.bakeVertices(level, bounds, new Vector3f());
                mrs.updateScene(bakedLevel);
            });

            virtualLevel.setBounds(bounds);
            blocks.placeInWorld(virtualLevel, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings().setKnownShape(true), RandomSource.create(), Block.UPDATE_CLIENTS);
            virtualLevel.refreshBlockEntityModels();

            var bakedLevel = LevelBakery.bakeVertices(virtualLevel, bounds, new Vector3f());
            mrs.updateScene(bakedLevel);

//            mrs.getMenu().setBlocks(blocks);
//            mrs.updateBlockRender();
        }
    }

}
