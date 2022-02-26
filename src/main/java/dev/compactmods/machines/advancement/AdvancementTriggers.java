package dev.compactmods.machines.advancement;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.trigger.ClaimedMachineTrigger;
import dev.compactmods.machines.advancement.trigger.HowDidYouGetHereTrigger;
import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.rooms.RoomSize;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementTriggers {

    public static final HowDidYouGetHereTrigger HOW_DID_YOU_GET_HERE = CriteriaTriggers.register(new HowDidYouGetHereTrigger());

    public static final ClaimedMachineTrigger CLAIMED_TINY = CriteriaTriggers.register(new ClaimedMachineTrigger(Advancements.CLAIMED_TINY_MACHINE));
    public static final ClaimedMachineTrigger CLAIMED_SMALL = CriteriaTriggers.register(new ClaimedMachineTrigger(Advancements.CLAIMED_SMALL_MACHINE));
    public static final ClaimedMachineTrigger CLAIMED_NORMAL = CriteriaTriggers.register(new ClaimedMachineTrigger(Advancements.CLAIMED_NORMAL_MACHINE));
    public static final ClaimedMachineTrigger CLAIMED_LARGE = CriteriaTriggers.register(new ClaimedMachineTrigger(Advancements.CLAIMED_LARGE_MACHINE));
    public static final ClaimedMachineTrigger CLAIMED_GIANT = CriteriaTriggers.register(new ClaimedMachineTrigger(Advancements.CLAIMED_GIANT_MACHINE));
    public static final ClaimedMachineTrigger CLAIMED_MAX = CriteriaTriggers.register(new ClaimedMachineTrigger(Advancements.CLAIMED_MAX_MACHINE));

    public static void init() {
        CompactMachines.LOGGER.trace("Registering advancement triggers.");
    }

    public static ClaimedMachineTrigger getTriggerForMachineClaim(RoomSize machineSize) {
        switch (machineSize) {
            case TINY: return CLAIMED_TINY;
            case SMALL: return CLAIMED_SMALL;
            case NORMAL: return CLAIMED_NORMAL;
            case LARGE: return CLAIMED_LARGE;
            case GIANT: return CLAIMED_GIANT;
            case MAXIMUM: return CLAIMED_MAX;
        }

        return CLAIMED_TINY;
    }
}
