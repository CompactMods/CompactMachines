package dev.compactmods.machines.room.preview;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A lightweight, per-frame-ish snapshot of one entity inside a tracked room, for the live preview.
 *
 * <p>Only what the preview needs to place and draw the entity: a stable {@link #entityId} (so the
 * client can match it frame-to-frame for interpolation), its {@link #type}, its position in
 * <em>room-local</em> space (relative to the room's inner-bounds minimum corner — the same local
 * frame the block mesh uses), and its rotation. Sent frequently on its own packet, separate from the
 * (much slower) block snapshot.
 */
public record RoomEntitySnapshot(int entityId, EntityType<?> type,
                                 float x, float y, float z,
                                 float yaw, float pitch, float headYaw, boolean onGround,
                                 @Nullable UUID playerId, @Nullable String playerName) {

    private static final StreamCodec<RegistryFriendlyByteBuf, EntityType<?>> TYPE_CODEC =
            ByteBufCodecs.registry(Registries.ENTITY_TYPE);

    public static final StreamCodec<RegistryFriendlyByteBuf, RoomEntitySnapshot> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RoomEntitySnapshot decode(RegistryFriendlyByteBuf buf) {
                    final int id = ByteBufCodecs.VAR_INT.decode(buf);
                    final EntityType<?> type = TYPE_CODEC.decode(buf);
                    final float x = buf.readFloat(), y = buf.readFloat(), z = buf.readFloat();
                    final float yaw = buf.readFloat(), pitch = buf.readFloat(), headYaw = buf.readFloat();
                    final boolean onGround = buf.readBoolean();
                    UUID playerId = null;
                    String playerName = null;
                    if (buf.readBoolean()) { // is a player
                        playerId = buf.readUUID();
                        playerName = buf.readUtf();
                    }
                    return new RoomEntitySnapshot(id, type, x, y, z, yaw, pitch, headYaw, onGround, playerId, playerName);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RoomEntitySnapshot v) {
                    ByteBufCodecs.VAR_INT.encode(buf, v.entityId);
                    TYPE_CODEC.encode(buf, v.type);
                    buf.writeFloat(v.x);
                    buf.writeFloat(v.y);
                    buf.writeFloat(v.z);
                    buf.writeFloat(v.yaw);
                    buf.writeFloat(v.pitch);
                    buf.writeFloat(v.headYaw);
                    buf.writeBoolean(v.onGround);
                    final boolean isPlayer = v.playerId != null && v.playerName != null;
                    buf.writeBoolean(isPlayer);
                    if (isPlayer) {
                        buf.writeUUID(v.playerId);
                        buf.writeUtf(v.playerName);
                    }
                }
            };

    public static final StreamCodec<RegistryFriendlyByteBuf, java.util.List<RoomEntitySnapshot>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list());
}
