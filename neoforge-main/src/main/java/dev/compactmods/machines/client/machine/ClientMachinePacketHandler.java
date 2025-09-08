package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.client.config.ClientConfig;
import dev.compactmods.machines.client.room.MachineRoomScreen;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.machine.OpenMachinePreviewScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.concurrent.CompletableFuture;

public class ClientMachinePacketHandler {
    public static void setMachineColor(GlobalPos position, MachineColor newColor) {
        var mc = Minecraft.getInstance();
        assert mc.level != null;
        if (mc.level.dimension() == position.dimension()) {
            var state = mc.level.getBlockState(position.pos());
            var blockEntity = mc.level.getBlockEntity(position.pos());

            // state.is(MachineConstants.MACHINE_BLOCK)
            if(blockEntity instanceof ICompactMachineBlockEntity cmbe) {
                cmbe.setMachineColor(newColor);
                mc.level.sendBlockUpdated(position.pos(), state, state, Block.UPDATE_ALL_IMMEDIATE);
            }
        }
    }

    public static void openRoomPreviewScreen(OpenMachinePreviewScreenPacket pkt) {
        final var mc = Minecraft.getInstance();
        mc.setScreen(new MachineRoomScreen(Component.empty(), pkt.machinePos(), pkt.roomCode()));
        if(mc.screen instanceof MachineRoomScreen mrs && ClientConfig.ENABLE_ROOM_PREVIEWS.get()) {
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
