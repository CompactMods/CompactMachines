package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import dev.compactmods.machines.room.PlayerRoomMetadataProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Provider provider. Because Java.
public class PlayerRoomMetadataProviderProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private static final Capability<IPlayerRoomMetadataProvider> CURRENT_ROOM_META = CapabilityManager.get(new CapabilityToken<>() {
    });

    private final IPlayerRoomMetadataProvider provider;

    private final LazyOptional<IPlayerRoomMetadataProvider> lazy;

    public PlayerRoomMetadataProviderProvider() {
        provider = new PlayerRoomMetadataProvider();
        lazy = LazyOptional.of(this::getCurrentRoomMetadataProvider);
    }

    private IPlayerRoomMetadataProvider getCurrentRoomMetadataProvider() {
        return this.provider;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CURRENT_ROOM_META)
            return lazy.cast();

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
