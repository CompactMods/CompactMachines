package dev.compactmods.machines.client.room;

import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ClientRoomPacketHandler {
    public static void handleBlockData(StructureTemplate blocks) {
        final var mc = Minecraft.getInstance();
        if(mc.gui.screen() instanceof MachineRoomScreen) {

            //            var virtualLevel = new VirtualLevel(Minecraft.getInstance().level.registryAccess(), true, level -> {
//                level.refreshBlockEntityModels();
//
//                var bakedLevel = LevelBakery.bakeVertices(level, bounds, new Vector3f());
//                mrs.updateScene(bakedLevel);
//            });
//
//            virtualLevel.setBounds(bounds);
//            blocks.placeInWorld(virtualLevel, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings().setKnownShape(true), RandomSource.create(), Block.UPDATE_CLIENTS);
//            virtualLevel.refreshBlockEntityModels();
//
//            var bakedLevel = LevelBakery.bakeVertices(virtualLevel, bounds, new Vector3f());
//            mrs.updateScene(bakedLevel);

//            mrs.getMenu().setBlocks(blocks);
//            mrs.updateBlockRender();
        }
    }
}
