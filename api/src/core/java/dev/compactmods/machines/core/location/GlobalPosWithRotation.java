package dev.compactmods.machines.core.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record GlobalPosWithRotation(ResourceKey<Level> dimension, Vec3 position, Vec2 rotation) {

    /*
     * BlockPos also technically supported here, since pos matches GlobalPos.CODEC#pos - only difference
     * here is that BlockPos uses an INT stream, while VEC3D uses a double stream.
     */
    public static final Codec<GlobalPosWithRotation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(GlobalPosWithRotation::dimension),
            Vec3.CODEC.fieldOf("pos").forGetter(GlobalPosWithRotation::position),
            Vec2.CODEC.optionalFieldOf("rot", Vec2.ZERO).forGetter(x -> x.rotation)
    ).apply(i, GlobalPosWithRotation::new));

    public static final GlobalPosWithRotation INVALID = new GlobalPosWithRotation(GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO), Vec2.ZERO);

    public GlobalPosWithRotation(GlobalPos pos, Vec2 rotation) {
        this(pos.dimension(), Vec3.atBottomCenterOf(pos.pos()), rotation);
    }

    public static GlobalPosWithRotation fromPlayer(Player player) {
        return new GlobalPosWithRotation(player.level().dimension(), player.position(), new Vec2(player.xRotO, player.yRotO));
    }

    @Override
    public String toString() {
        return "GlobalPosWRot {%s; pos = %s, rot = %s".formatted(dimension.identifier(), position, rotation);
    }
}
