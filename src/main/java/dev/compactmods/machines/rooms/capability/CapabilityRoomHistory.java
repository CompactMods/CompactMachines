package dev.compactmods.machines.rooms.capability;

import javax.annotation.Nullable;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityRoomHistory {

    @CapabilityInject(IRoomHistory.class)
    public static Capability<IRoomHistory> HISTORY_CAPABILITY = null;

    public static class CapabilityRoomHistoryStorage implements Capability.IStorage<IRoomHistory> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<IRoomHistory> capability, IRoomHistory instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IRoomHistory> capability, IRoomHistory instance, Direction side, INBT nbt) {
            if(nbt instanceof ListNBT)
                instance.deserializeNBT((ListNBT) nbt);
        }
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IRoomHistory.class, new CapabilityRoomHistoryStorage(), CMRoomHistory::new);
    }
}
