package dev.compactmods.machines.room.preview.client.gui;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

/**
 * The preview camera's fog, written into a {@code Fog} uniform block (layout as {@link FogRenderer}).
 * Clear outside fluids; inside water it uses the camera biome's water fog, inside lava vanilla's
 * spectator lava fog — the preview camera passes through everything, like a spectator.
 *
 * <p>{@link #update} writes a GPU buffer, so it must run outside a render pass.
 */
final class PreviewFog implements AutoCloseable {

    /** Fully transparent, as vanilla's "no fog" buffer. */
    private static final Vector4fc NO_FOG = new Vector4f(0f, 0f, 0f, 0f);
    private static final Vector4fc LAVA_COLOR = ARGB.vector4fFromARGB32(0xFF990A00);
    private static final float LAVA_START = -8f, LAVA_END = 24f;
    /** Pushes the render-distance fog (unused here) out of reach. */
    private static final float FAR = 1.0e6f;

    private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
            () -> CompactMachinesCore.dotPrefix("room_preview_fog"),
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, FogRenderer.FOG_UBO_SIZE);

    GpuBuffer buffer() {
        return buffer;
    }

    /**
     * Writes the fog for a camera at room-local {@code camera}.
     *
     * @return the colour to clear the preview to — the fog colour when in a fluid, so the void fades
     * into it too; otherwise transparent
     */
    Vector4fc update(RoomPreviewSnapshot snapshot, Vector3fc camera) {
        final int bx = Mth.floor(camera.x()), by = Mth.floor(camera.y()), bz = Mth.floor(camera.z());
        final FluidState fluid = submergedIn(snapshot, bx, by, bz, camera.y() - by);

        if (fluid != null && fluid.is(FluidTags.WATER)) {
            final Biome biome = biomeAt(snapshot, bx, by, bz);
            final var attributes = biome != null ? biome.getAttributes() : null;
            final var color = attribute(attributes, EnvironmentAttributes.WATER_FOG_COLOR);
            final float start = attribute(attributes, EnvironmentAttributes.WATER_FOG_START_DISTANCE);
            final float end = attribute(attributes, EnvironmentAttributes.WATER_FOG_END_DISTANCE);
            final Vector4f fogColor = new Vector4f(color.x(), color.y(), color.z(), 1f);
            write(fogColor, start, end);
            return fogColor;
        }

        if (fluid != null && fluid.is(FluidTags.LAVA)) {
            write(LAVA_COLOR, LAVA_START, LAVA_END);
            return LAVA_COLOR;
        }

        write(NO_FOG, FAR, FAR);
        return NO_FOG;
    }

    /** {@return the fluid the camera is under the surface of}, or {@code null} if none. */
    private static @Nullable FluidState submergedIn(RoomPreviewSnapshot snapshot, int bx, int by, int bz, float heightInBlock) {
        final FluidState fluid = snapshot.blockAt(bx, by, bz).getFluidState();
        if (fluid.isEmpty())
            return null;

        // A fluid with the same fluid above fills its block; otherwise only up to its surface
        final boolean filled = snapshot.blockAt(bx, by + 1, bz).getFluidState().getType().isSame(fluid.getType());
        return filled || heightInBlock <= fluid.getOwnHeight() ? fluid : null;
    }

    private static <T> T attribute(net.minecraft.world.attribute.@Nullable EnvironmentAttributeMap attributes,
                                   net.minecraft.world.attribute.EnvironmentAttribute<T> attribute) {
        return attributes != null
                ? attributes.applyModifier(attribute, attribute.defaultValue())
                : attribute.defaultValue();
    }

    private static @Nullable Biome biomeAt(RoomPreviewSnapshot snapshot, int bx, int by, int bz) {
        final var key = snapshot.biomes().biomeAt(bx, by, bz);
        final var level = Minecraft.getInstance().level;
        if (key == null || level == null)
            return null;

        return level.registryAccess().lookupOrThrow(Registries.BIOME).get(key).map(Holder::value).orElse(null);
    }

    private void write(Vector4fc color, float environmentalStart, float environmentalEnd) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final var data = Std140Builder.onStack(stack, FogRenderer.FOG_UBO_SIZE)
                    .putVec4(color)
                    .putFloat(environmentalStart)
                    .putFloat(environmentalEnd)
                    .putFloat(FAR)                // render-distance start
                    .putFloat(FAR)                // render-distance end
                    .putFloat(environmentalEnd)   // sky end
                    .putFloat(environmentalEnd)   // clouds end
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
        }
    }

    @Override
    public void close() {
        buffer.close();
    }
}
