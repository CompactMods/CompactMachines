package dev.compactmods.machines.machine.client.render;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class VerticesHelper {
    public static GpuBuffer uploadVertices(@Nullable GpuBuffer existing, MeshData mesh, Supplier<String> label) {
        if (existing == null || existing.size() < mesh.vertexBuffer().remaining()) {
            if (existing != null) existing.close();

            return RenderSystem.getDevice()
                    .createBuffer(
                            label,
                            GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX,
                            mesh.vertexBuffer());
        }

        RenderSystem.getDevice()
                .createCommandEncoder()
                .writeToBuffer(existing.slice(), mesh.vertexBuffer());

        return existing;
    }

    public static GpuBuffer uploadIndices(@Nullable GpuBuffer existing, MeshData mesh, Supplier<String> label) {
        if (existing == null || existing.size() < mesh.indexBuffer().remaining()) {
            if (existing != null) existing.close();

            return RenderSystem.getDevice()
                    .createBuffer(
                            label,
                            GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
                            mesh.indexBuffer()
                    );
        }

        RenderSystem.getDevice()
                .createCommandEncoder()
                .writeToBuffer(existing.slice(), mesh.indexBuffer());

        return existing;
    }
}
