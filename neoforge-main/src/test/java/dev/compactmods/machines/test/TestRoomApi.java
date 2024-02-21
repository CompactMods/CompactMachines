package dev.compactmods.machines.test;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.IRoomApi;
import dev.compactmods.machines.room.RoomApiInstance;
import net.minecraft.gametest.framework.GameTestHelper;

public class TestRoomApi {

    public static IRoomApi forTest(GameTestHelper test) {
        try {
            var defaultInst = RoomApiInstance.forServer(test.getLevel().getServer());
        return new RoomApiInstance(Predicates.alwaysTrue(), defaultInst.registrar(), null, defaultInst.spawnManagers(), defaultInst.chunkManager());
        } catch (MissingDimensionException e) {
            test.fail(e.getMessage());
            return null;
        }
    }
}
