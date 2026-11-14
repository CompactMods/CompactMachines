package dev.compactmods.machines.shrinking.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/// A component that allows for interaction with Compact Machine machine blocks.
/// If this is added to an item, it allows a player to "shrink" into the machine room via the item.
///
/// @param topLevelOnly If true, only allows a player to enter a single room before disallowing more teleports.
/// @param afterUseAction How the item should react after a successful teleport into a room.
public record ShrinkingDeviceConfiguration(boolean topLevelOnly, AfterUseAction afterUseAction) {

    public static final Codec<ShrinkingDeviceConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("top_level_only").forGetter(ShrinkingDeviceConfiguration::topLevelOnly),
            AfterUseAction.CODEC.fieldOf("after_use").forGetter(ShrinkingDeviceConfiguration::afterUseAction)
    ).apply(instance, ShrinkingDeviceConfiguration::new));

    public static final StreamCodec<ByteBuf, ShrinkingDeviceConfiguration> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ShrinkingDeviceConfiguration::topLevelOnly,
            AfterUseAction.STREAM_CODEC, ShrinkingDeviceConfiguration::afterUseAction,
            ShrinkingDeviceConfiguration::new
    );

    public static ShrinkingDeviceConfiguration basicNoDamage() {
        return new ShrinkingDeviceConfiguration(false, AfterUseAction.DO_NOTHING);
    }

    public static ShrinkingDeviceConfiguration basicDamaging() {
        return new ShrinkingDeviceConfiguration(false, AfterUseAction.TRY_DAMAGE_ITEM);
    }

    public static ShrinkingDeviceConfiguration basicOneOff() {
        return new ShrinkingDeviceConfiguration(false, AfterUseAction.DESTROY_ITEM);
    }

    public static ShrinkingDeviceConfiguration basicOneOffTopLevel() {
        return new ShrinkingDeviceConfiguration(true, AfterUseAction.DESTROY_ITEM);
    }

    public static final ShrinkingDeviceConfiguration DEFAULT_CONFIG = basicNoDamage();

    public enum AfterUseAction implements StringRepresentable {
        DO_NOTHING,
        TRY_DAMAGE_ITEM,
        DESTROY_ITEM;

        public static final Codec<AfterUseAction> CODEC = StringRepresentable.fromValues(AfterUseAction::values);

        public static final StreamCodec<ByteBuf, AfterUseAction> STREAM_CODEC =
                ByteBufCodecs.fromCodecTrusted(StringRepresentable.fromValues(AfterUseAction::values));

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
