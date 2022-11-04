package dev.compactmods.machines.advancement;

import dev.compactmods.machines.ICompactMachinesMod;
import dev.compactmods.machines.advancement.trigger.BasicPlayerAdvTrigger;
import dev.compactmods.machines.advancement.trigger.HowDidYouGetHereTrigger;
import dev.compactmods.machines.api.core.Advancements;

public class AdvancementTriggers {

    public static final BasicPlayerAdvTrigger RECURSIVE_ROOMS = new BasicPlayerAdvTrigger(Advancements.RECURSIVE_ROOMS);

    public static final HowDidYouGetHereTrigger HOW_DID_YOU_GET_HERE = new HowDidYouGetHereTrigger();

    public static final BasicPlayerAdvTrigger CLAIMED_TINY = new BasicPlayerAdvTrigger(Advancements.CLAIMED_TINY_MACHINE);
    public static final BasicPlayerAdvTrigger CLAIMED_SMALL = new BasicPlayerAdvTrigger(Advancements.CLAIMED_SMALL_MACHINE);
    public static final BasicPlayerAdvTrigger CLAIMED_NORMAL = new BasicPlayerAdvTrigger(Advancements.CLAIMED_NORMAL_MACHINE);
    public static final BasicPlayerAdvTrigger CLAIMED_LARGE = new BasicPlayerAdvTrigger(Advancements.CLAIMED_LARGE_MACHINE);
    public static final BasicPlayerAdvTrigger CLAIMED_GIANT = new BasicPlayerAdvTrigger(Advancements.CLAIMED_GIANT_MACHINE);
    public static final BasicPlayerAdvTrigger CLAIMED_MAX = new BasicPlayerAdvTrigger(Advancements.CLAIMED_MAX_MACHINE);

    public static void init() {
        ICompactMachinesMod.LOGGER.trace("Registering advancement triggers.");
    }
}
