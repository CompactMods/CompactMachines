package dev.compactmods.machines.shrinking.api.capability;

import dev.compactmods.machines.shrinking.api.history.RoomEntryMethod;
import dev.compactmods.machines.shrinking.api.history.RoomEntryResult;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;

import java.util.concurrent.CompletableFuture;

public interface PlayerShrinkingHandler {

    CompletableFuture<RoomEntryResult> tryEnter(RoomEntryMethod method);

    CompletableFuture<RoomExitResult> tryExit();

}

