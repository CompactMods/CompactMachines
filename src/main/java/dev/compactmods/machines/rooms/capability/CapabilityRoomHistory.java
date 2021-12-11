package dev.compactmods.machines.rooms.capability;

import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityRoomHistory {

    public static Capability<IRoomHistory> HISTORY_CAPABILITY = CapabilityManager.get(new CapabilityToken<IRoomHistory>() {
    });

    public static class CapabilityRoomHistoryStorage implements INBTSerializable<ListTag> {

        private final IRoomHistory history;

        public CapabilityRoomHistoryStorage(IRoomHistory history) {
            this.history = history;
        }

        @Override
        public ListTag serializeNBT() {
            return history.serializeNBT();
        }

        @Override
        public void deserializeNBT(ListTag nbt) {
            history.deserializeNBT(nbt);
        }
    }
}
