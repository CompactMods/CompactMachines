package dev.compactmods.machines.advancement;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.trigger.BasicPlayerAdvTrigger;
import dev.compactmods.machines.advancement.trigger.HowDidYouGetHereTrigger;
import dev.compactmods.machines.api.Advancements;
import dev.compactmods.machines.api.room.RoomSize;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementTriggers {

    public static final BasicPlayerAdvTrigger RECURSIVE_ROOMS = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.RECURSIVE_ROOMS));

    public static final HowDidYouGetHereTrigger HOW_DID_YOU_GET_HERE = CriteriaTriggers.register(new HowDidYouGetHereTrigger());

    public static final BasicPlayerAdvTrigger CLAIMED_TINY = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.CLAIMED_TINY_MACHINE));
    public static final BasicPlayerAdvTrigger CLAIMED_SMALL = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.CLAIMED_SMALL_MACHINE));
    public static final BasicPlayerAdvTrigger CLAIMED_NORMAL = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.CLAIMED_NORMAL_MACHINE));
    public static final BasicPlayerAdvTrigger CLAIMED_LARGE = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.CLAIMED_LARGE_MACHINE));
    public static final BasicPlayerAdvTrigger CLAIMED_GIANT = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.CLAIMED_GIANT_MACHINE));
    public static final BasicPlayerAdvTrigger CLAIMED_MAX = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.CLAIMED_MAX_MACHINE));

    public static void init() {
        CompactMachines.LOGGER.trace("Registering advancement triggers.");
    }

    public static BasicPlayerAdvTrigger getTriggerForMachineClaim(RoomSize machineSize) {
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
