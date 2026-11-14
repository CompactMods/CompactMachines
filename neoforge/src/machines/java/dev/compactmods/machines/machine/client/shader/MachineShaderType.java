package dev.compactmods.machines.machine.client.shader;

import com.mojang.serialization.MapCodec;

public record MachineShaderType<T extends MachineShader>(MapCodec<T> codec) {}
