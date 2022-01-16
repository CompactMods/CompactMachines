package dev.compactmods.machines.api.tunnels.lifecycle;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IPersistentTunnelData<T extends Tag> extends INBTSerializable<T> { }

