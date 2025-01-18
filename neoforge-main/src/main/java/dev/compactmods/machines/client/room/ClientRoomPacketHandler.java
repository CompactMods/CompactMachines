package dev.compactmods.machines.client.room;

import dev.compactmods.gander.level.VirtualLevel;
import dev.compactmods.gander.render.geometry.LevelBakery;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Vector3f;

import java.util.UUID;

public class ClientRoomPacketHandler {
    public static void handleBlockData(StructureTemplate blocks) {
        final var mc = Minecraft.getInstance();
        if(mc.screen instanceof MachineRoomScreen mrs) {

            var virtualLevel = new VirtualLevel(Minecraft.getInstance().level.registryAccess(), true);
            var bounds = blocks.getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO);
            virtualLevel.setBounds(bounds);
            blocks.placeInWorld(virtualLevel, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings().setKnownShape(true), RandomSource.create(), Block.UPDATE_CLIENTS);

            var bakedLevel = LevelBakery.bakeVertices(virtualLevel, bounds, new Vector3f());

            mrs.updateScene(bakedLevel);

//            mrs.getMenu().setBlocks(blocks);
//            mrs.updateBlockRender();
        }
    }

    public static void handleRoomSync(String roomCode, UUID owner) {
        final var mc = Minecraft.getInstance();

        // FIXME - Current Room Owner
        mc.player.setData(Rooms.DataAttachments.CURRENT_ROOM_CODE, roomCode);
        // mc.player.setData(Rooms.DataAttachments)
    }
}
