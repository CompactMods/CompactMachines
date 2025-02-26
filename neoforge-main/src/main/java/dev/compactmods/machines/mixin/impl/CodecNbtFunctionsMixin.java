package dev.compactmods.machines.mixin.impl;

import dev.compactmods.machines.mixin.CodecNbtFunctions;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CompoundTag.class, priority = 999, remap = false)
public abstract class CodecNbtFunctionsMixin implements CodecNbtFunctions {
}
