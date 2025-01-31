package dev.compactmods.machines.api;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.CompactRoomGenerator;
import dev.compactmods.machines.api.room.IRoomApi;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.room.data.IRoomDataAttachmentAccessor;
import dev.compactmods.machines.api.room.history.IPlayerHistoryApi;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeAccessor;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeType;
import dev.compactmods.machines.api.room.upgrade.data.IRoomUpgradeDataAttachmentAccessor;
import dev.compactmods.machines.api.util.BlockSpaceUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

public class CompactMachines {
	public final static String MOD_ID = "compactmachines";

	private static final Logger logger = LogManager.getLogger();

	//region API Cache - NO TOUCHY - This class gives access to these services, or you can service locate them yourself!
	@ApiStatus.Internal
	private static IRoomApi ROOM_API;

	@ApiStatus.Internal
	private static IRoomDataAttachmentAccessor ROOM_DATA_ACCESSOR;

	@ApiStatus.Internal
	private static IPlayerHistoryApi PLAYER_HISTORY_API;

	@ApiStatus.Internal
	private static IRoomUpgradeAccessor UPGRADE_ACCESSOR;

	@ApiStatus.Internal
	private static IRoomUpgradeDataAttachmentAccessor ROOM_UPGRADE_DATA_ACCESSOR;
	//endregion

	/**
	 * Reloads the references to the services this class uses.
	 * Typically called after a new server fires its starting event; API consumers SHOULD NOT need
	 * to call this!
	 */
	public static void reloadServices() {
		reloadServices("dev.compactmods.machines");
	}

	public static void reloadServices(String prefix) {
		logger.debug("Reloading Compact services...");
		ROOM_API = cmService(IRoomApi.class, prefix);
		ROOM_DATA_ACCESSOR = cmService(IRoomDataAttachmentAccessor.class, prefix);
		PLAYER_HISTORY_API = cmService(IPlayerHistoryApi.class, prefix);
		UPGRADE_ACCESSOR = cmService(IRoomUpgradeAccessor.class, prefix);
		ROOM_UPGRADE_DATA_ACCESSOR = cmService(IRoomUpgradeDataAttachmentAccessor.class, prefix);
		logger.debug("Compact services loaded.");
	}

	/**
	 * Uses service locator to try and find the implementations housed in the main mod JAR.
	 * No touchy; we expose the desired services via locator and methods here.
	 * @param serviceClass
	 * @return
	 * @param <T>
	 */
	private static <T> T cmService(Class<T> serviceClass, String packagePrefix) {
		final var loader = ServiceLoader.load(serviceClass);
		logger.debug("Attempting to find implementation for {}...", serviceClass.getName());

		for(var s : loader) {
			if(s.getClass().getPackageName().startsWith(packagePrefix)) {
				logger.debug("Located implementation for {}: {}", serviceClass.getName(), s.getClass().getName());
				return s;
			}
		}

		logger.error("Could not find implementation for {}! Did the mod jar corrupt?", serviceClass.getName());
		return null;
	}

	public static String id(String path) {
		return ResourceLocation.isValidPath(path) ? (MOD_ID + ":" + path) : MOD_ID + ":invalid";
	}

	public static String dotPrefix(String path) {
		return MOD_ID + "." + path;
	}

	public static ResourceLocation modRL(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static DeferredRegister<RoomUpgradeType<?>> roomUpgradeDR(String namespace) {
		return DeferredRegister.create(RoomUpgradeType.REGISTRY_KEY, namespace);
	}

	public static IRoomApi roomApi() {
		return ROOM_API;
	}

	public static IPlayerHistoryApi playerHistoryApi() {
		return PLAYER_HISTORY_API;
	}

	public static Optional<RoomInstance> room(String roomCode) {
		return ROOM_API.registrar().get(roomCode);
	}

	/**
	 * Registers a new room instance and generates the structure in the compact world.
	 *
	 * @param server   Server to generate room on.
	 * @param template
	 * @param owner
	 * @return
	 */
	public static RoomInstance newRoom(MinecraftServer server, RoomTemplate template, UUID owner) throws MissingDimensionException {
		final var instance = ROOM_API.registrar().createNew(template, owner);
		final var compactDim = CompactDimension.forServer(server);
		CompactRoomGenerator.generateRoom(compactDim, instance.boundaries().outerBounds());

		if (!template.structures().isEmpty()) {
			for (var struct : template.structures()) {
				CompactRoomGenerator.populateStructure(compactDim, struct.template(), instance.boundaries().innerBounds(), struct.placement());
			}
		}

		final var spawnManager = ROOM_API.spawnManager(instance.code());
		template.optionalFloor().ifPresent(floorState -> {
			var fixedSpawn = instance.boundaries()
				.defaultSpawn()
				.add(0, 1, 0);

			spawnManager.setDefaultSpawn(fixedSpawn, Vec2.ZERO);

			AABB floorBounds = BlockSpaceUtil.getWallBounds(instance.boundaries().innerBounds(), Direction.DOWN);
			BlockSpaceUtil.blocksInside(floorBounds).forEach(floorBlockPos -> {
				compactDim.setBlock(floorBlockPos, floorState, Block.UPDATE_ALL);
			});
		});

		return instance;
	}

	public static boolean isValidRoomCode(String roomCode) {
		return ROOM_API.roomCodeValidator().test(roomCode);
	}

	public static Optional<? extends IAttachmentHolder> existingRoomData(String code) {
		return ROOM_DATA_ACCESSOR.get(code);
	}

	public static IRoomDataAttachmentAccessor roomDataAccessor() {
		return ROOM_DATA_ACCESSOR;
	}

	public static IAttachmentHolder roomData(String code) {
		return ROOM_DATA_ACCESSOR.getOrCreate(code);
	}

	public static IRoomUpgradeAccessor upgradeAccessor() {
		return UPGRADE_ACCESSOR;
	}

	public static IRoomUpgradeDataAttachmentAccessor upgradeDataAccessor() {
		return ROOM_UPGRADE_DATA_ACCESSOR;
	}

	public static IAttachmentHolder roomUpgradeData(String roomCode, UUID upgradeId) {
		return ROOM_UPGRADE_DATA_ACCESSOR.getOrCreate(roomCode, upgradeId);
	}
}
