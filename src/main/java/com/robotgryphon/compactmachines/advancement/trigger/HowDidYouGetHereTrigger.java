package com.robotgryphon.compactmachines.advancement.trigger;

import com.google.gson.JsonObject;
import com.robotgryphon.compactmachines.advancement.GenericAdvancementTriggerListener;
import com.robotgryphon.compactmachines.advancement.GenericAdvancementTriggerListenerList;
import com.robotgryphon.compactmachines.api.core.Advancements;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public class HowDidYouGetHereTrigger implements ICriterionTrigger<HowDidYouGetHereTrigger.Instance> {

    private final GenericAdvancementTriggerListenerList<HowDidYouGetHereTrigger.Instance> listeners = new GenericAdvancementTriggerListenerList<>();

    @Override
    public ResourceLocation getId() {
        return Advancements.HOW_DID_YOU_GET_HERE;
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
        return new Instance(EntityPredicate.AndPredicate.fromJson(json, "player", conditions));
    }

    public void trigger(ServerPlayerEntity player) {
        final GenericAdvancementTriggerListener<Instance> listeners = this.listeners.getListeners(player);
        if(listeners != null)
            listeners.trigger();
    }

    public static class Instance extends CriterionInstance {

        public Instance(EntityPredicate.AndPredicate player) {
            super(Advancements.HOW_DID_YOU_GET_HERE, player);
        }

        public static Instance create() {
            return new Instance(EntityPredicate.AndPredicate.ANY);
        }
    }
}
