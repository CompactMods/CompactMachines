package dev.compactmods.machines.test.services;

import dev.compactmods.machines.api.room.history.IPlayerEntryPointHistoryManager;
import dev.compactmods.machines.api.room.history.IPlayerHistoryApi;
import dev.compactmods.machines.player.PlayerEntryPointHistoryManager;

public class TestPlayerHistoryApi implements IPlayerHistoryApi {
    @Override
    public IPlayerEntryPointHistoryManager entryPoints() {
        return new PlayerEntryPointHistoryManager(5);
    }

    @Override
    public void save() {

    }
}
