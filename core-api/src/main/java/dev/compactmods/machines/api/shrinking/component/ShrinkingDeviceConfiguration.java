package dev.compactmods.machines.api.shrinking.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.codec.CodecExtensions;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

public record ShrinkingDeviceConfiguration(
        int maxAllowedDepth,
        boolean allowLoops,
        AfterUseAction afterUseAction
) {
    public static final int ALLOWED_DEPTH_MIN = 1;
    public static final int ALLOWED_DEPTH_DEFAULT = 5;
    public static final int ALLOWED_DEPTH_MAX = 25;

    public static final Codec<ShrinkingDeviceConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(ALLOWED_DEPTH_MIN, ALLOWED_DEPTH_MAX).fieldOf("max_allowed_depth").forGetter(ShrinkingDeviceConfiguration::maxAllowedDepth),
            Codec.BOOL.fieldOf("allow_loops").forGetter(ShrinkingDeviceConfiguration::allowLoops),
            AfterUseAction.CODEC.fieldOf("after_use").forGetter(ShrinkingDeviceConfiguration::afterUseAction)
    ).apply(instance, ShrinkingDeviceConfiguration::new));

    public static final StreamCodec<ByteBuf, ShrinkingDeviceConfiguration> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ShrinkingDeviceConfiguration::maxAllowedDepth,
            ByteBufCodecs.BOOL, ShrinkingDeviceConfiguration::allowLoops,
            AfterUseAction.STREAM_CODEC, ShrinkingDeviceConfiguration::afterUseAction,
            ShrinkingDeviceConfiguration::new
    );

    public static ShrinkingDeviceConfiguration basicNoDamage(int maxAllowedDepth) {
        int clamped = Mth.clamp(maxAllowedDepth, ALLOWED_DEPTH_MIN, ALLOWED_DEPTH_MAX);
        return new ShrinkingDeviceConfiguration(clamped, true, AfterUseAction.DO_NOTHING);
    }

    public static ShrinkingDeviceConfiguration basicDamaging(int maxAllowedDepth) {
        int clamped = Mth.clamp(maxAllowedDepth, ALLOWED_DEPTH_MIN, ALLOWED_DEPTH_MAX);
        return new ShrinkingDeviceConfiguration(clamped, true, AfterUseAction.DAMAGE);
    }

    public static ShrinkingDeviceConfiguration basicOneOff() {
        return new ShrinkingDeviceConfiguration(ALLOWED_DEPTH_MAX, true, AfterUseAction.BREAK);
    }

    public static final ShrinkingDeviceConfiguration DEFAULT_CONFIG = basicNoDamage(ALLOWED_DEPTH_DEFAULT);

    public enum AfterUseAction implements StringRepresentable {
        DO_NOTHING,
        DAMAGE,
        BREAK;

        public static final Codec<AfterUseAction> CODEC = StringRepresentable.fromValues(AfterUseAction::values);

        public static final StreamCodec<ByteBuf, AfterUseAction> STREAM_CODEC = CodecExtensions.stringRepresentableStreamCodec(AfterUseAction.values());

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
