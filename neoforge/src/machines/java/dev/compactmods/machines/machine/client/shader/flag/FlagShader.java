package dev.compactmods.machines.machine.client.shader.flag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.machine.client.shader.MachineShader;
import dev.compactmods.machines.machine.client.shader.MachineShaderType;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

/// A coloured-stripe flag that the Compact Machine can wear via the
/// `pride_stripes` shader. [#colors] is in _top-to-bottom_
/// order so the list reads the same way the flag itself does, and they're stored
/// as 24-bit packed RGB (`0xRRGGBB`) &mdash; easy to construct from hex codes
/// and trivially codec-able once the registry moves to datapacks.
///
/// Limit: ten colours. Anything past that gets ignored because the shader
/// declares a fixed `vec3[10]` palette array.
///
/// @param colors stripe colours, top-to-bottom, packed `0xRRGGBB`.
public record FlagShader(List<Integer> colors) implements MachineShader {

    public static final int MAX_STRIPES = 10;

    public static final ResourceKey<Registry<FlagShader>> REGISTRY_KEY;

    static {
        REGISTRY_KEY = ResourceKey.createRegistryKey(CompactMachinesCore.identifier("flag_shaders"));
    }

    public int size() {
        return Math.min(colors.size(), MAX_STRIPES);
    }

    /** Codec for future datapack loading; not wired into a real registry yet. */
    public static final Codec<FlagShader> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ExtraCodecs.STRING_RGB_COLOR.listOf().fieldOf("colors").forGetter(FlagShader::colors)
    ).apply(inst, FlagShader::new));

    @Override
    public MachineShaderType<?> type() {
        return null;
    }
}
