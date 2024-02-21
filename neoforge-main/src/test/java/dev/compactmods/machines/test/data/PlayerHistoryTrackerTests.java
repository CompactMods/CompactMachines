package dev.compactmods.machines.test.data;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.IRoomApi;
import dev.compactmods.machines.api.room.IRoomRegistrar;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.api.room.owner.IRoomOwners;
import dev.compactmods.machines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.player.PlayerEntryPointHistory;
import dev.compactmods.machines.player.RoomEntryResult;
import dev.compactmods.machines.room.RoomApiInstance;
import dev.compactmods.machines.room.RoomCodeGenerator;
import dev.compactmods.machines.room.RoomRegistrar;
import dev.compactmods.machines.test.TestRoomApi;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class PlayerHistoryTrackerTests {

    private static final String BATCH = "PLAYER_HISTORY_TRACKING";

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void failsPlayerGoingTooFar(final GameTestHelper test) {
        RoomApi.INSTANCE = TestRoomApi.forTest(test);

        final var history = new PlayerEntryPointHistory(1);

        final var player = test.makeMockSurvivalPlayer();
        history.enterRoom(player, RoomCodeGenerator.generateRoomId(), RoomEntryPoint.nonexistent());

        final var tooFar = history.enterRoom(player, RoomCodeGenerator.generateRoomId(), RoomEntryPoint.nonexistent());

        test.assertTrue(tooFar == RoomEntryResult.FAILED_TOO_FAR_DOWN, "Room entry should have failed.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = BATCH, timeoutTicks = 1400)
    public static void canGetPlayerHistory(final GameTestHelper test) throws InterruptedException {
        RoomApi.INSTANCE = TestRoomApi.forTest(test);

        final var history = new PlayerEntryPointHistory(5);

        final var player = test.makeMockSurvivalPlayer();

        Deque<String> codes = new ArrayDeque<>(5);
        for (int i = 0; i < 5; i++) {
            var roomId = RoomCodeGenerator.generateRoomId();
            codes.push(roomId);
            history.enterRoom(player, roomId, RoomEntryPoint.nonexistent());

            Thread.sleep(250);
        }

        var hist = history.history(player)
                .limit(3)
                .collect(Collectors.toCollection(ArrayDeque::new));

        test.assertTrue(hist.size() == 3, "Expected 3 entries in history.");

        var mostRecent = hist.pop();
        test.assertTrue(mostRecent.roomCode().equals(codes.peek()), "Latest room code does not match.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = BATCH, timeoutTicks = 1400)
    public static void canRemovePlayerHistory(final GameTestHelper test) throws InterruptedException {
        RoomApi.INSTANCE = TestRoomApi.forTest(test);

        final var history = new PlayerEntryPointHistory(5);
        final var player = test.makeMockSurvivalPlayer();

        Deque<String> codes = new ArrayDeque<>(5);
        for (int i = 0; i < 5; i++) {
            var roomId = RoomCodeGenerator.generateRoomId();
            codes.push(roomId);
            history.enterRoom(player, roomId, RoomEntryPoint.nonexistent());

            Thread.sleep(250);
        }

        var oldHistoryAmount = history.history(player).count();

        history.popHistory(player, 1);

        var newHistoryAmount = history.history(player).count();

        test.assertTrue(oldHistoryAmount > newHistoryAmount, "History amount was not correct after pop.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = BATCH, timeoutTicks = 1400)
    public static void testDataLogic(final GameTestHelper test) throws InterruptedException {
        final var history = new PlayerEntryPointHistory(5);

        final var player = test.makeMockSurvivalPlayer();

        Deque<String> codes = new ArrayDeque<>(5);
        for (int i = 0; i < 5; i++) {
            var roomId = RoomCodeGenerator.generateRoomId();
            codes.push(roomId);
            history.enterRoom(player, roomId, RoomEntryPoint.nonexistent());

            Thread.sleep(250);
        }

        long beforeSave = history.history(player.getUUID()).count();

        final var saved = history.save(new CompoundTag());

        final var loaded = PlayerEntryPointHistory.CODEC.parse(NbtOps.INSTANCE, saved)
                .getOrThrow(false, test::fail);

        long afterLoad = loaded.history(player.getUUID()).count();

        test.assertTrue(beforeSave == afterLoad, "History counts differ.");

        test.succeed();
    }
}
