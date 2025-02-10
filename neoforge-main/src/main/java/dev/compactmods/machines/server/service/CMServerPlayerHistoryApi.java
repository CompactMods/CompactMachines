package dev.compactmods.machines.server.service;

import dev.compactmods.machines.api.room.history.IPlayerEntryPointHistoryManager;
import dev.compactmods.machines.api.room.history.IPlayerHistoryApi;
import dev.compactmods.machines.data.manager.CMSingletonDataFileManager;
import dev.compactmods.machines.player.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.server.CompactMachinesServer;

public class CMServerPlayerHistoryApi implements IPlayerHistoryApi {

    private final CMSingletonDataFileManager<PlayerEntryPointHistoryManager> PLAYER_HISTORY_DATA;

    public CMServerPlayerHistoryApi() {
        PLAYER_HISTORY_DATA = new CMSingletonDataFileManager<>(CompactMachinesServer.currentServer(), "player_entrypoint_history", new PlayerEntryPointHistoryManager(5));
        PLAYER_HISTORY_DATA.load();
    }

    @Override
    public IPlayerEntryPointHistoryManager entryPoints() {
        return PLAYER_HISTORY_DATA.data();
    }

    @Override
    public void save() {
        PLAYER_HISTORY_DATA.save();
    }
}
