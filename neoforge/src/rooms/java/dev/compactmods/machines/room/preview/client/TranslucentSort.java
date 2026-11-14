package dev.compactmods.machines.room.preview.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.IndexType;
import dev.compactmods.machines.core.CompactMachinesCore;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Back-to-front ordering for a translucent preview layer. Translucent quads write depth, so drawn
 * in bake order a nearer quad can reject a farther one behind it (e.g. water's overlay faces against
 * glass) depending on the view. Like vanilla chunk sections, the quads' centroids are kept and the
 * index buffer is rebuilt farthest-first whenever the camera moves.
 *
 * <p>{@link #sortFor} writes a GPU buffer, so it must run outside a render pass.
 */
public final class TranslucentSort implements AutoCloseable {

    /** Re-sort once the camera has moved this far (mesh-local blocks) since the last sort. */
    private static final float RESORT_DISTANCE_SQ = 0.25f * 0.25f;

    private final MeshData.SortState state;
    private final GpuBuffer indexBuffer;
    private final int indexCount;
    private final Vector3f lastCamera = new Vector3f(Float.NaN);

    /** @param initialIndices the index buffer {@link MeshData#sortQuads} produced alongside {@code state} */
    TranslucentSort(MeshData.SortState state, ByteBuffer initialIndices, int indexCount) {
        this.state = state;
        this.indexCount = indexCount;
        this.indexBuffer = RenderSystem.getDevice().createBuffer(
                () -> CompactMachinesCore.dotPrefix("room_preview_translucent_indices"),
                GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, initialIndices);
    }

    /** Re-orders the quads back-to-front from {@code camera} (mesh-local) if it moved enough. */
    public void sortFor(Vector3fc camera) {
        if (lastCamera.isFinite() && lastCamera.distanceSquared(camera) < RESORT_DISTANCE_SQ)
            return;
        lastCamera.set(camera);

        final ByteBuffer sorted = MemoryUtil.memAlloc(indexCount * state.indexType().bytes);
        try {
            state.writeSortedIndexBuffer(sorted, VertexSorting.byDistance(camera));
            sorted.flip();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(indexBuffer.slice(), sorted);
        } finally {
            MemoryUtil.memFree(sorted);
        }
    }

    public GpuBuffer indexBuffer() {
        return indexBuffer;
    }

    public IndexType indexType() {
        return state.indexType();
    }

    @Override
    public void close() {
        indexBuffer.close();
    }
}
