package dev.compactmods.machines.upgrades.storage.tunnel.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.machines.room.preview.client.RoomPreviewOverlay;
import dev.compactmods.machines.upgrades.storage.tunnel.RoomStorageLinks;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelTranslations;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.upgrades.storage.tunnel.preview.ClientTunnelMarkers;
import dev.compactmods.machines.upgrades.storage.tunnel.preview.TunnelMarker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shows a room's tunnels in the room preview screen: a see-through copy of each tunnel wall, a short
 * stub leading out of the room from it, and a sphere in the tunnel type's color at the stub's end.
 * Hovering the sphere shows the storage and the machines exposing it.
 */
public final class TunnelPreviewOverlay implements RoomPreviewOverlay {

    public static final TunnelPreviewOverlay INSTANCE = new TunnelPreviewOverlay();

    private static final int WALL_ALPHA = 0x80;
    private static final float STUB_LENGTH = 0.75f;
    private static final float STUB_HALF_WIDTH = 0.0625f;
    private static final float SPHERE_RADIUS = 0.2f;
    private static final float HOVERED_SCALE = 1.25f;
    /** Pick radius over the drawn one, so small spheres are easy to hover. */
    private static final float PICK_PADDING = 0.1f;
    private static final int SPHERE_STACKS = 8, SPHERE_SLICES = 12;

    /** Hover id: a tunnel's storage name, kept distinct from other overlays' ids. */
    private record Hovered(String storage) {}

    private final QuadInstance quad = new QuadInstance();
    private final List<BlockStateModelPart> parts = new ArrayList<>();

    private TunnelPreviewOverlay() {}

    @Override
    public void submit(String roomCode, PoseStack pose, SubmitNodeCollector collector, @Nullable Object hovered, float partialTick) {
        final var markers = ClientTunnelMarkers.get(roomCode);
        if (markers.isEmpty())
            return;

        final var wall = wallParts();
        for (TunnelMarker marker : markers) {
            final boolean isHovered = new Hovered(marker.name()).equals(hovered);
            final BlockPos at = marker.local();

            pose.pushPose();
            pose.translate(at.getX(), at.getY(), at.getZ());
            collector.submitCustomGeometry(pose, RenderTypes.translucentMovingBlock(),
                    (p, buffer) -> putWall(p, buffer, wall, marker.color()));
            pose.popPose();

            collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (p, buffer) -> {
                putStub(p, buffer, marker);
                putSphere(p, buffer, sphereCenter(marker), SPHERE_RADIUS * (isHovered ? HOVERED_SCALE : 1f),
                        isHovered ? brighten(marker.color()) : marker.color());
            });
        }
    }

    @Override
    public Optional<Hit> pick(String roomCode, Vector3fc origin, Vector3fc direction) {
        Hit best = null;
        for (TunnelMarker marker : ClientTunnelMarkers.get(roomCode)) {
            final float distance = raySphere(origin, direction, sphereCenter(marker), SPHERE_RADIUS + PICK_PADDING);
            if (distance >= 0 && (best == null || distance < best.distance()))
                best = new Hit(new Hovered(marker.name()), distance, tooltip(marker));
        }
        return Optional.ofNullable(best);
    }

    @Override
    public void clear() {
        ClientTunnelMarkers.clear();
    }

    // --- geometry ---------------------------------------------------------------------

    /** The tunnel wall model's parts, in block space. */
    private List<BlockStateModelPart> wallParts() {
        parts.clear();
        final var state = Tunnels.Blocks.TUNNEL_WALL.get().defaultBlockState();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state)
                .collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RandomSource.create(42L), parts);
        return List.copyOf(parts);
    }

    /** The wall model at half opacity, its tinted overlay in the tunnel type's color. */
    private void putWall(PoseStack.Pose pose, VertexConsumer buffer, List<BlockStateModelPart> wall, int color) {
        quad.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
        quad.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        final int base = ARGB.color(WALL_ALPHA, 0xFFFFFF);
        final int tinted = ARGB.color(WALL_ALPHA, color);
        for (BlockStateModelPart part : wall) {
            for (Direction side : Direction.values())
                part.getQuads(side).forEach(q -> putQuad(pose, buffer, q, base, tinted));
            part.getQuads(null).forEach(q -> putQuad(pose, buffer, q, base, tinted));
        }
    }

    private void putQuad(PoseStack.Pose pose, VertexConsumer buffer, BakedQuad q, int base, int tinted) {
        quad.setColor(q.materialInfo().tintIndex() == 0 ? tinted : base);
        buffer.putBakedQuad(pose, q, quad);
    }

    /** {@return the centre of the wall's outer face} */
    private static Vector3f outerFace(TunnelMarker marker) {
        final var normal = marker.outward().getUnitVec3f();
        return new Vector3f(marker.local().getX() + 0.5f, marker.local().getY() + 0.5f, marker.local().getZ() + 0.5f)
                .fma(0.5f, normal);
    }

    private static Vector3f sphereCenter(TunnelMarker marker) {
        return outerFace(marker).fma(STUB_LENGTH + SPHERE_RADIUS, marker.outward().getUnitVec3f());
    }

    /** A thin box from the wall's outer face, out along its normal. */
    private static void putStub(PoseStack.Pose pose, VertexConsumer buffer, TunnelMarker marker) {
        final Vector3f start = outerFace(marker);
        final Vector3f end = new Vector3f(start).fma(STUB_LENGTH, marker.outward().getUnitVec3f());
        final Vector3f min = new Vector3f(start).min(end).sub(STUB_HALF_WIDTH, STUB_HALF_WIDTH, STUB_HALF_WIDTH);
        final Vector3f max = new Vector3f(start).max(end).add(STUB_HALF_WIDTH, STUB_HALF_WIDTH, STUB_HALF_WIDTH);
        putBox(pose, buffer, min, max, ARGB.color(0xE0, ARGB.scaleRGB(marker.color(), 0.8f)));
    }

    private static void putBox(PoseStack.Pose pose, VertexConsumer buffer, Vector3f min, Vector3f max, int color) {
        final float x0 = min.x, y0 = min.y, z0 = min.z, x1 = max.x, y1 = max.y, z1 = max.z;
        final int top = color, side = shade(color, 0.8f), bottom = shade(color, 0.5f);
        quad(pose, buffer, top, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        quad(pose, buffer, bottom, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(pose, buffer, side, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(pose, buffer, side, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        quad(pose, buffer, side, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(pose, buffer, side, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    /** A latitude/longitude sphere, shaded as if lit from above. */
    private static void putSphere(PoseStack.Pose pose, VertexConsumer buffer, Vector3f center, float radius, int color) {
        final Vector3f[][] points = new Vector3f[SPHERE_STACKS + 1][SPHERE_SLICES + 1];
        for (int i = 0; i <= SPHERE_STACKS; i++) {
            final float phi = Mth.PI * i / SPHERE_STACKS;
            for (int j = 0; j <= SPHERE_SLICES; j++) {
                final float theta = Mth.TWO_PI * j / SPHERE_SLICES;
                points[i][j] = new Vector3f(Mth.sin(phi) * Mth.cos(theta), Mth.cos(phi), Mth.sin(phi) * Mth.sin(theta));
            }
        }

        for (int i = 0; i < SPHERE_STACKS; i++) {
            for (int j = 0; j < SPHERE_SLICES; j++) {
                final Vector3f a = points[i][j], b = points[i][j + 1], c = points[i + 1][j + 1], d = points[i + 1][j];
                final float up = (a.y + b.y + c.y + d.y) / 4f;
                final int shaded = shade(color, 0.65f + 0.35f * up);
                quad(pose, buffer, shaded,
                        center.x + a.x * radius, center.y + a.y * radius, center.z + a.z * radius,
                        center.x + b.x * radius, center.y + b.y * radius, center.z + b.z * radius,
                        center.x + c.x * radius, center.y + c.y * radius, center.z + c.z * radius,
                        center.x + d.x * radius, center.y + d.y * radius, center.z + d.z * radius);
            }
        }
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        buffer.addVertex(pose, ax, ay, az).setColor(color);
        buffer.addVertex(pose, bx, by, bz).setColor(color);
        buffer.addVertex(pose, cx, cy, cz).setColor(color);
        buffer.addVertex(pose, dx, dy, dz).setColor(color);
    }

    /** {@code color} (opaque unless it has alpha) with its RGB scaled by {@code factor}. */
    private static int shade(int color, float factor) {
        final int alpha = ARGB.alpha(color) == 0 ? 0xFF : ARGB.alpha(color);
        return ARGB.color(alpha, ARGB.scaleRGB(color, factor));
    }

    private static int brighten(int color) {
        return ARGB.srgbLerp(0.4f, ARGB.opaque(color), 0xFFFFFFFF);
    }

    /** {@return the distance along the ray to the sphere}, or -1 if it misses (or is behind) */
    static float raySphere(Vector3fc origin, Vector3fc direction, Vector3fc center, float radius) {
        final Vector3f toCenter = new Vector3f(center).sub(origin);
        final float along = toCenter.dot(direction);
        final float missSq = toCenter.lengthSquared() - along * along;
        final float radiusSq = radius * radius;
        if (missSq > radiusSq)
            return -1f;

        final float distance = along - Mth.sqrt(radiusSq - missSq);
        return distance >= 0 ? distance : (along >= 0 ? 0f : -1f);
    }

    // --- tooltip ----------------------------------------------------------------------

    private static List<Component> tooltip(TunnelMarker marker) {
        final var lines = new ArrayList<Component>();
        lines.add(Component.literal(marker.name()).withColor(marker.color()));
        lines.add(Component.translatable(TunnelType.translationKey(marker.type().identifier())).withStyle(ChatFormatting.GRAY));

        if (marker.links().isEmpty()) {
            lines.add(TunnelTranslations.Preview.NO_CONNECTIONS.get().copy().withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }

        lines.add(TunnelTranslations.Preview.CONNECTED.get());
        for (RoomStorageLinks.Link link : marker.links()) {
            final var pos = link.machine().pos();
            final String faces = link.faces().stream()
                    .map(face -> StringUtils.capitalize(face.getSerializedName()))
                    .collect(Collectors.joining(", "));
            lines.add(Component.literal("  ").append(TunnelTranslations.Preview.MACHINE.apply(new Object[]{
                    pos.getX(), pos.getY(), pos.getZ(), link.machine().dimension().identifier().toString(), faces
            })).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
