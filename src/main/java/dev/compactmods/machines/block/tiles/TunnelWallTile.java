//package dev.compactmods.machines.block.tiles;
//
//import dev.compactmods.machines.CompactMachines;
//import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
//import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
//import dev.compactmods.machines.api.tunnels.ICapableTunnel;
//import dev.compactmods.machines.api.tunnels.TunnelDefinition;
//import dev.compactmods.machines.block.walls.TunnelWallBlock;
//import dev.compactmods.machines.core.Registration;
//import dev.compactmods.machines.data.persistent.CompactMachineData;
//import dev.compactmods.machines.data.persistent.MachineConnections;
//import dev.compactmods.machines.network.NetworkHandler;
//import dev.compactmods.machines.network.TunnelAddedPacket;
//import dev.compactmods.machines.teleportation.DimensionalPosition;
//import dev.compactmods.machines.tunnels.TunnelHelper;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.world.level.block.entity.TickableBlockEntity;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.core.Direction;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.chunk.LevelChunk;
//import net.minecraft.world.server.ServerWorld;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.util.LazyOptional;
//import net.minecraftforge.fml.common.registry.GameRegistry;
//import net.minecraftforge.fml.network.PacketDistributor;
//import net.minecraftforge.items.CapabilityItemHandler;
//import net.minecraftforge.items.IItemHandler;
//import net.minecraftforge.items.ItemStackHandler;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//public class TunnelWallTile extends BlockEntity implements TickableBlockEntity {
//
//    private int connectedMachine;
//    private ResourceLocation tunnelType;
//
//    private final HashMap<Capability<?>, LazyOptional<?>> capabilityCache;
//
//    public TunnelWallTile() {
//        super(Registration.TUNNEL_WALL_TILE.get());
//        this.capabilityCache = new HashMap<>();
//    }
//
//    @Override
//    public void load(BlockState state, CompoundTag nbt) {
//        super.load(state, nbt);
//
//        if (nbt.contains("tunnel_type")) {
//            ResourceLocation type = new ResourceLocation(nbt.getString("tunnel_type"));
//            this.tunnelType = type;
//        }
//
//        if (nbt.contains("machine")) {
//            this.connectedMachine = nbt.getInt("machine");
//        }
//    }
//
//    @Override
//    public CompoundTag save(CompoundTag compound) {
//        compound = super.save(compound);
//        compound.putString("tunnel_type", tunnelType.toString());
//        compound.putInt("machine", connectedMachine);
//        return compound;
//    }
//
//    @Override
//    public CompoundTag getUpdateTag() {
//        CompoundTag nbt = super.getUpdateTag();
//        nbt.putString("tunnel_type", tunnelType.toString());
//        nbt.putInt("machine", connectedMachine);
//        return nbt;
//    }
//
//    @Override
//    public void handleUpdateTag(BlockState state, CompoundTag tag) {
//        super.handleUpdateTag(state, tag);
//
//        if (tag.contains("tunnel_type")) {
//            this.tunnelType = new ResourceLocation(tag.getString("tunnel_type"));
//        }
//
//        if (tag.contains("machine")) {
//            this.connectedMachine = tag.getInt("machine");
//        }
//    }
//
//    public Optional<IDimensionalPosition> getConnectedPosition() {
//        return Optional.empty();
//    }
//
//    private Optional<Integer> tryFindExternalMachineByChunkPos(MachineConnections connections) {
//        ChunkPos thisMachineChunk = new ChunkPos(worldPosition);
//        Collection<Integer> externalMachineIDs = connections.graph.getMachinesFor(thisMachineChunk);
//
//        // This shouldn't happen - there should always be at least one machine attached externally
//        // If this DOES happen, it's probably a migration failure or the block was destroyed without notification
//        if (externalMachineIDs.isEmpty()) {
//            CompactMachines.LOGGER.warn("Warning: Tunnel applied to a machine but no external machine data found.");
//            CompactMachines.LOGGER.warn("Please validate the tunnel at: " + worldPosition.toShortString());
//            return Optional.empty();
//        }
//
//        int first = externalMachineIDs.stream().findFirst().orElse(-1);
//        // sanity - makes compiler happier, we already did a check above for empty state
//        if (first == -1) return Optional.empty();
//
//        // In theory, we can re-attach the tunnel to the first found external machine, if the saved data
//        // does not actually contain an attached external id
//        return Optional.of(first);
//    }
//
//    /**
//     * Gets the side the tunnel is placed on (the wall inside the machine)
//     *
//     * @return
//     */
//    public Direction getTunnelSide() {
//        BlockState state = getBlockState();
//        return state.getValue(TunnelWallBlock.TUNNEL_SIDE);
//    }
//
//    /**
//     * Gets the side the tunnel connects to externally (the machine side)
//     *
//     * @return
//     */
//    public Direction getConnectedSide() {
//        BlockState blockState = getBlockState();
//        return blockState.getValue(TunnelWallBlock.CONNECTED_SIDE);
//    }
//
//    public Optional<ResourceLocation> getTunnelDefinitionId() {
//        return Optional.ofNullable(this.tunnelType);
//    }
//
//    public Optional<TunnelDefinition> getTunnelDefinition() {
//        if (tunnelType == null)
//            return Optional.empty();
//
//        TunnelDefinition definition = GameRegistry
//                .findRegistry(TunnelDefinition.class)
//                .getValue(tunnelType);
//
//        return Optional.ofNullable(definition);
//    }
//
//    public void setTunnelType(ResourceLocation registryName) {
//        this.tunnelType = registryName;
//
//        if (level != null && !level.isClientSide()) {
//            setChanged();
//
//            TunnelAddedPacket pkt = new TunnelAddedPacket(worldPosition, registryName);
//
//            LevelChunk chunkAt = level.getChunkAt(worldPosition);
//            NetworkHandler.MAIN_CHANNEL
//                    .send(PacketDistributor.TRACKING_CHUNK.with(() -> chunkAt), pkt);
//        }
//    }
//
//    @Override
//    public void tick() {
//        // first tick - unset this block, tunnels will be reimplemented in 1.17
//        level.setBlockAndUpdate(worldPosition, Registration.BLOCK_SOLID_WALL.get().defaultBlockState());
//    }
//}
