package dev.compactmods.machines.core.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;

public record MachineColor(int red, int green, int blue) {

    public static final MachineColor DEFAULT = fromDyeColor(DyeColor.WHITE);
    public static final MachineColor WHITE = MachineColor.fromARGB(CommonColors.WHITE);

    public static MachineColor fromARGB(int argb) {
        var red = ARGB.red(argb);
        var green = ARGB.green(argb);
        var blue = ARGB.blue(argb);
        return new MachineColor(red, green, blue);
    }

    public static final Codec<MachineColor> INT_CODEC = Codec.INT.xmap(MachineColor::fromARGB, MachineColor::rgb);

    public static final Codec<MachineColor> HEX_CODEC = Codec.STRING.comapFlatMap(str -> {
        if (!str.startsWith("#")) {
            return DataResult.error(() -> "Not a color code: " + str);
        } else {
            final var stripped = str.substring(1);
            if(stripped.length() != 6)
                return DataResult.error(() -> "Not a color code: " + stripped);

            try {
                int i = (int)Long.parseLong(stripped, 16);
                return DataResult.success(MachineColor.fromARGB(i));
            } catch (NumberFormatException nfe) {
                return DataResult.error(() -> "Exception parsing color code: " + nfe.getMessage());
            }
        }
    }, MachineColor::formatValue);

    public static final Codec<MachineColor> CODEC = Codec.withAlternative(HEX_CODEC, INT_CODEC);

    public static final StreamCodec<ByteBuf, MachineColor> STREAM_CODEC = ByteBufCodecs.INT.map(MachineColor::fromARGB, MachineColor::rgb);

    public static MachineColor fromDyeColor(DyeColor color) {
        return MachineColor.fromARGB(color.getFireworkColor());
    }

    public int rgb() {
        return ARGB.color(red, green, blue);
    }

    private String formatValue() {
        return "#" + String.format(Locale.ROOT, "%08X", this.rgb()).substring(2);
    }

}
