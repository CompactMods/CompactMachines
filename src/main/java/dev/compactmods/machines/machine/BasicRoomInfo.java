package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.room.registration.IBasicRoomInfo;

public record BasicRoomInfo(String code, int color) implements IBasicRoomInfo {
}
