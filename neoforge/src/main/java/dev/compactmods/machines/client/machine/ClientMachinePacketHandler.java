package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.client.config.ClientConfig;
import dev.compactmods.machines.client.room.MachineRoomScreen;
import dev.compactmods.machines.network.machine.OpenMachinePreviewScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientMachinePacketHandler {

    public static void openRoomPreviewScreen(OpenMachinePreviewScreenPacket pkt) {
        final var mc = Minecraft.getInstance();
        mc.gui.setScreen(new MachineRoomScreen(Component.empty(), pkt.machinePos(), pkt.roomCode()));
        if(mc.gui.screen() instanceof MachineRoomScreen && ClientConfig.ENABLE_ROOM_PREVIEWS.get()) {
//            CompletableFuture<BakedLevel> setup = CompletableFuture.supplyAsync(() -> {
//                var virtualLevel = new VirtualLevel(Minecraft.getInstance().level.registryAccess(), true);
//                var bounds = AABB.of(pkt.internalBlocks().getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO));
//                virtualLevel.setBounds(bounds);
//                pkt.internalBlocks().placeInWorld(virtualLevel, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings().setKnownShape(true), RandomSource.create(), Block.UPDATE_CLIENTS);
//
//                var bakedLevel = LevelBakery.bakeVertices(virtualLevel, bounds, new Vector3f());
//                return bakedLevel;
//            });
//
//            mrs.updateSceneRenderer(setup);
        }
    }
}
