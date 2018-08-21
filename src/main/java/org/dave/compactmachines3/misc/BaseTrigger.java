package org.dave.compactmachines3.misc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseTrigger implements ICriterionTrigger<BaseTrigger.Instance> {
    private final ResourceLocation ID;
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.<PlayerAdvancements, BaseTrigger.Listeners>newHashMap();

    public BaseTrigger(String parString) {
        super();
        ID = new ResourceLocation(parString);
    }

    public BaseTrigger(ResourceLocation parRL) {
        super();
        ID = parRL;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<BaseTrigger.Instance> listener) {
        BaseTrigger.Listeners basetrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (basetrigger$listeners == null) {
            basetrigger$listeners = new BaseTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, basetrigger$listeners);
        }

        basetrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<BaseTrigger.Instance> listener) {
        BaseTrigger.Listeners basetrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (basetrigger$listeners != null) {
            basetrigger$listeners.remove(listener);

            if (basetrigger$listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public BaseTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new BaseTrigger.Instance(this.getId());
    }

    public void trigger(EntityPlayerMP parPlayer) {
        BaseTrigger.Listeners basetrigger$listeners = this.listeners.get(parPlayer.getAdvancements());

        if (basetrigger$listeners != null) {
            basetrigger$listeners.trigger(parPlayer);
        }
    }

    public static class Instance extends AbstractCriterionInstance {

        public Instance(ResourceLocation parID) {
            super(parID);
        }

        public boolean test() {
            return true;
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.<ICriterionTrigger.Listener<BaseTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<BaseTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<BaseTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(EntityPlayerMP player) {
            List<Listener<Instance>> list = null;

            for (ICriterionTrigger.Listener<BaseTrigger.Instance> listener : this.listeners) {
                if (listener.getCriterionInstance().test()) {
                    if (list == null) {
                        list = Lists.<ICriterionTrigger.Listener<BaseTrigger.Instance>>newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (ICriterionTrigger.Listener<BaseTrigger.Instance> listener1 : list) {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}