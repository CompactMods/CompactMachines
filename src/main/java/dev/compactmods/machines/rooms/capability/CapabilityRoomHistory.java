package dev.compactmods.machines.rooms.capability;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityRoomHistory {

    @CapabilityInject(IRoomHistory.class)
    public static Capability<IRoomHistory> HISTORY_CAPABILITY = null;

    public static void setup() {
        CapabilityManager.INSTANCE.register(
                IRoomHistory.class,
                new Capability.IStorage<IRoomHistory>() {
                    @Nullable
                    @Override
                    public INBT writeNBT(Capability<IRoomHistory> capability, IRoomHistory instance, Direction side) {
                        return new CompoundNBT();
                    }

                    @Override
                    public void readNBT(Capability<IRoomHistory> capability, IRoomHistory instance, Direction side, INBT nbt) {

                    }
                }, CMRoomHistory::new);
    }
}
