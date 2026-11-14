package dev.compactmods.machines.room.generation;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.generation.*;
import dev.compactmods.machines.api.room.generation.RoomStructureInfo.RoomStructurePlacement;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.core.WallConstants;
import dev.compactmods.machines.core.util.BlockSpaceUtil;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.spatial.aabb.AABBAligner;
import dev.compactmods.spatial.aabb.AABBHelper;
import dev.compactmods.spatial.vector.VectorUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;

import java.util.Map;
import java.util.Optional;

public class ServerRoomGenerator implements RoomGenerator {

    private final MinecraftServer server;
    private final ServerLevel level;
    private final RoomRegistry roomRegistry;
    private final Map<Integer, ServerNewRoomBuilder> pendingReservations;

    public ServerRoomGenerator(MinecraftServer server, RoomRegistry roomRegistry) {
        this.server = server;
        this.level = CompactDimension.forServer(server);
        this.roomRegistry = roomRegistry;
        this.pendingReservations = new Int2ObjectOpenHashMap<>();
    }

    /// Generates a wall or platform in a given direction. Uses the solid wall block.
    ///
    /// @param outerBounds
    /// @param wallDirection
    /// @since 3.0.0
    public void generateCompactWall(AABB outerBounds, Direction wallDirection, BlockState block) {
        AABB wallBounds = BlockSpaceUtil.getWallBounds(outerBounds, wallDirection);
        BlockSpaceUtil.blocksInside(wallBounds).forEach(wallBlock -> {
            level.setBlock(wallBlock, block, Block.UPDATE_ALL);
        });
    }

    /// Generates a machine "internal" structure in a world via a machine size and a central point.
    ///
    /// @param outerBounds Outer dimensions of the room.
    public void generateRoom(AABB outerBounds) {
        final var block = BuiltInRegistries.BLOCK.getValue(WallConstants.SOLID_WALL);
        if (block != null) {
            final var solidWall = block.defaultBlockState();
            generateRoom(outerBounds, solidWall);
        }
    }

    /**
     * Generates a machine structure in a world via machine boundaries and a wall block.
     *
     * @param outerBounds Outer dimensions of the room.
     * @param block       Block to use for walls.
     */
    public void generateRoom(AABB outerBounds, BlockState block) {

        // Generate the walls
        for (final var dir : Direction.values())
            generateCompactWall(outerBounds, dir, block);

        // Clear out the inside of the room
        AABB machineInternal = outerBounds.deflate(1);
        BlockSpaceUtil.blocksInside(machineInternal)
                .forEach(p -> level.setBlock(p, Blocks.AIR.defaultBlockState(), 7));
    }

    public void populateStructure(Identifier template, AABB roomInnerBounds, RoomStructurePlacement placement) {
        server.getStructureTemplateManager().get(template).ifPresent(tem -> {

            Vector3d templateSize = VectorUtils.convert3d(tem.getSize());

            if (!AABBHelper.fitsInside(roomInnerBounds, templateSize)) {
                // skip: structure too large to place in room
                return;
            }

            var placementSettings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE);

            AABB placementBounds = null;
            final var aligner = AABBAligner.create(roomInnerBounds, AABBHelper.zeroOriginSized(templateSize));
            switch (placement) {
                case RoomStructurePlacement.CENTERED -> placementBounds = aligner
                        .center(roomInnerBounds.getCenter())
                        .align();

                case RoomStructurePlacement.CENTERED_CEILING -> placementBounds = aligner
                        .boundedDirection(Direction.UP)
                        .align();

                case RoomStructurePlacement.CENTERED_FLOOR -> placementBounds = aligner
                        .boundedDirection(Direction.DOWN)
                        .align();
            }

            if (placementBounds != null) {
                final var pos = AABBHelper.minCorner(placementBounds);
                BlockPos placeAt = BlockPos.containing(pos.x(), pos.y(), pos.z());
                tem.placeInWorld(level, placeAt, placeAt, placementSettings, level.getRandom(), Block.UPDATE_ALL);
            }
        });
    }

    @Override
    public NewRoomBuilder createNew() {

        // Rooms generate in a spiral algorithm.
        // The next position should be the number of registered rooms plus
        // the number of rooms that are still being generated (pendingReservations)
        final int spiralIndex = roomRegistry.count() + pendingReservations.size();

        final var builder = new ServerNewRoomBuilder(spiralIndex);
        pendingReservations.put(spiralIndex, builder);
        return builder;

//        throw new RoomGenerationException("Failed to reserve room code [%s]; refusing to continue with generation!".formatted(newCode));
    }

    @Override
    public Optional<RoomGenerationResult> generate(RoomGenerationDetails details) throws RoomGenerationException {
        if (!details.template().isBound())
            throw new RoomGenerationException("Template must be bound!");

        final var newRoomBoundaries = details.boundaries();
        final var template = details.template().value();

        // If we are not allowing big rooms, exit
        if(template.internalDimensions().maxDimension() > 45) {
            final var allowingBigRooms = server.getGameRules().get(CMGameRules.ALLOW_BIG_ROOMS.value());
            if (!allowingBigRooms)
                return Optional.empty();
        }

        // Empty Room (Box)
        generateRoom(newRoomBoundaries.outerBounds());

        if (!template.structures().isEmpty()) {
            for (var struct : template.structures()) {
                populateStructure(struct.template(), newRoomBoundaries.innerBounds(), struct.placement());
            }
        }

        template.optionalFloor().ifPresent(floorState -> {
            // Generate the floor
            AABB floorBounds = BlockSpaceUtil.getWallBounds(newRoomBoundaries.innerBounds(), Direction.DOWN);
            BlockSpaceUtil.blocksInside(floorBounds).forEach(floorBlockPos -> {
                level.setBlock(floorBlockPos, floorState, Block.UPDATE_ALL);
            });

            // Bump default spawn up 1 block to account for floor
//            final var spawnManager = spawnManagers.get(newCode);
//            var fixedSpawn = newRoomBoundaries
//                    .defaultSpawn()
//                    .add(0, 1, 0);
//
//            spawnManager.setDefaultSpawn(fixedSpawn, Vec2.ZERO);
        });

        // Assign room roomCode and return instance
        final var result = roomRegistry.register(details.template(), details.boundaries(), details.owner());

        return result.map(inst -> {
            // Inform the chunk manager to track new room chunks
            var chunkManager = server.getCapability(RoomCapabilities.CHUNK_MANAGER);
            if (chunkManager != null)
                chunkManager.calculateChunks(inst.code(), inst.boundaries());

            pendingReservations.remove(details.spiralIndex());

            return new RoomGenerationResult(inst.code(), newRoomBoundaries);
        });
    }
}
