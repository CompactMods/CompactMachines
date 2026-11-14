package dev.compactmods.machines.machine.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import dev.compactmods.machines.machine.Machines;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class MachineMeshHelper {
    private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_COLOR_NORMAL;
    private static final ByteBufferBuilder meshBuffer = new ByteBufferBuilder(VERTEX_FORMAT.getVertexSize() * 8);

    /**
     * Look at each of the six neighbour blocks. Bit set ⇒ neighbour is another
     * compact machine, so the BER should skip its overlay quad on that face
     */
    private static int computeNeighborMask(@Nullable Level level, BlockPos pos) {
        if (level == null) return 0;
        int mask = 0;
        var machineBlock = Machines.Blocks.MACHINE.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.setWithOffset(pos, dir);
            if (level.getBlockState(cursor).is(machineBlock)) {
                mask |= 1 << dir.get3DDataValue();
            }
        }
        return mask;
    }

    static @Nullable MeshData buildMesh(PoseStack stack, LongList positions, Frustum frustum) {
        var size = positions.size() * VERTEX_FORMAT.getVertexSize();
        if (size == 0) return null;

        var builder = new BufferBuilder(meshBuffer, PrimitiveTopology.QUADS, VERTEX_FORMAT);
        for (long position : positions) {
            var x = BlockPos.getX(position);
            var y = BlockPos.getY(position);
            var z = BlockPos.getZ(position);

            // add a cube
            stack.pushPose();
            stack.translate(-frustum.getCamX(), -frustum.getCamY(), -frustum.getCamZ());
            stack.translate(x, y, z);

            // TODO: Use computeMask above
            CompactMachineRenderer.emitPanes(stack.last(), builder, 0);
            stack.popPose();
        }

        var result = builder.build();
        if (result != null) result.sortQuads(meshBuffer, VertexSorting.DISTANCE_TO_ORIGIN);

        return result;
    }
}


