package dev.compactmods.machines.advancement.trigger;

import com.google.gson.JsonObject;
import dev.compactmods.machines.advancement.GenericAdvancementTriggerListener;
import dev.compactmods.machines.advancement.GenericAdvancementTriggerListenerList;
import dev.compactmods.machines.api.core.Advancements;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public class ClaimedMachineTrigger implements ICriterionTrigger<ClaimedMachineTrigger.Instance> {

    private final GenericAdvancementTriggerListenerList<Instance> listeners = new GenericAdvancementTriggerListenerList<>();
    private final ResourceLocation advancementId;

    public ClaimedMachineTrigger(ResourceLocation advancementId) {
        this.advancementId = advancementId;
    }

    @Override
    public ResourceLocation getId() {
        return advancementId;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements advancements, Listener<Instance> list) {
        listeners.addPlayerListener(advancements, list);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements advancements, Listener<Instance> list) {
        listeners.removePlayerListener(advancements, list);
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements advancements) {
        listeners.removePlayerListeners(advancements);
    }

    @Override
    public Instance createInstance(JsonObject json, ConditionArrayParser conditions) {
        return new Instance(this.advancementId, EntityPredicate.AndPredicate.fromJson(json, "player", conditions));
    }

    public void trigger(ServerPlayerEntity player) {
        final GenericAdvancementTriggerListener<Instance> listeners = this.listeners.getListeners(player);
        if(listeners != null)
            listeners.trigger();
    }

    public static class Instance extends CriterionInstance {

        private final ResourceLocation advId;

        public Instance(ResourceLocation advId, EntityPredicate.AndPredicate player) {
            super(advId, player);
            this.advId = advId;
        }

        public static Instance create(ResourceLocation advancement) {
            return new Instance(advancement, EntityPredicate.AndPredicate.ANY);
        }
    }
}
