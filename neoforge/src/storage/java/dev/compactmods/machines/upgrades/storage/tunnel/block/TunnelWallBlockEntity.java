package dev.compactmods.machines.upgrades.storage.tunnel.block;

import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A tunnel on a room wall: which named storage it's bound to, which way it faces into the room, and
 * its tunnel type's color (synced to clients for the wall's tint).
 *
 * <p>Exposes the bound storage's handler as the tunnel type's block capability, on the inward face
 * (or unsided), so blocks inside the room can move resources through it.
 */
public class TunnelWallBlockEntity extends BlockEntity {

    private String storageName = "";
    private @Nullable ResourceKey<TunnelType> type;
    private Direction face = Direction.NORTH;
    private int color = 0xFFFFFF;

    public TunnelWallBlockEntity(BlockPos pos, BlockState state) {
        super(Tunnels.BlockEntities.TUNNEL_WALL.get(), pos, state);
    }

    public String storageName() {
        return storageName;
    }

    public Optional<ResourceKey<TunnelType>> type() {
        return Optional.ofNullable(type);
    }

    public Direction face() {
        return face;
    }

    /** Tunnel type color, packed {@code 0xRRGGBB}. */
    public int color() {
        return color;
    }

    public void bind(String storageName, ResourceKey<TunnelType> type, Direction face, int color) {
        this.storageName = storageName;
        this.type = type;
        this.face = face;
        this.color = color;
        markChangedAndSync();
    }

    public void rename(String storageName) {
        this.storageName = storageName;
        markChangedAndSync();
    }

    /**
     * {@return the bound storage's handler for {@code capability}}, or null if this isn't the inward
     * face, the storage is gone, or its tunnel type exposes a different capability. Server only.
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T handler(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (level == null || level.isClientSide() || storageName.isEmpty() || type == null)
            return null;
        if (side != null && side != face)
            return null;

        return Tunnels.roomAt(level, worldPosition)
                .map(room -> room.getCapability(StorageCapabilities.NAMED_STORAGES))
                .filter(storages -> storages.definition(storageName).flatMap(def -> def.tunnel())
                        .map(worldPosition::equals).orElse(false))
                .filter(storages -> storages.tunnelType(type)
                        .flatMap(t -> t.value().resolveStorageType())
                        .map(storageType -> storageType.blockCapability() == capability)
                        .orElse(false))
                .flatMap(storages -> storages.handler(storageName))
                .map(handler -> (T) handler)
                .orElse(null);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        // However the wall goes away, its storage keeps its data but no longer has a tunnel.
        Tunnels.roomAt(level, pos).ifPresent(room -> room.getCapability(StorageCapabilities.NAMED_STORAGES).unbind(pos));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storageName = input.getStringOr("storage", "");
        this.type = input.read("type", ResourceKey.codec(TunnelType.REGISTRY_KEY)).orElse(null);
        this.face = input.read("face", Direction.CODEC).orElse(Direction.NORTH);
        this.color = input.read("color", ExtraCodecs.STRING_RGB_COLOR).orElse(0xFFFFFF);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("storage", storageName);
        output.storeNullable("type", ResourceKey.codec(TunnelType.REGISTRY_KEY), type);
        output.store("face", Direction.CODEC, face);
        output.store("color", ExtraCodecs.STRING_RGB_COLOR, color);
    }

    private void markChangedAndSync() {
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            final var state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag data = super.getUpdateTag(provider);
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(out);
        return data.merge(out.buildResult());
    }
}
