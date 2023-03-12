package dev.compactmods.machines.forge.data.generated;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.compactmods.machines.advancement.trigger.BasicPlayerAdvTrigger;
import dev.compactmods.machines.advancement.trigger.HowDidYouGetHereTrigger;
import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.shrinking.Shrinking;
import dev.compactmods.machines.forge.wall.Walls;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AdvancementGenerator implements DataProvider {

    private static final Logger LOGGER = LoggingUtil.modLog();

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;

    public AdvancementGenerator(DataGenerator gen) {
        this.generator = gen;
    }

    @Override
    public void run(@NotNull CachedOutput cache) {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (adv) -> {
            if (!set.add(adv.getId())) {
                throw new IllegalStateException("Duplicate advancement " + adv.getId());
            } else {
                Path path1 = path.resolve("data/" + adv.getId().getNamespace() + "/advancements/" + adv.getId().getPath() + ".json");

                try {
                    DataProvider.saveStable(cache, adv.deconstruct().serializeToJson(), path1);
                } catch (IOException ioexception) {
                    LOGGER.error("Couldn't save advancement {}", path1, ioexception);
                }

            }
        };

        generateAdvancements(consumer);
    }

    private ResourceLocation modLoc(String i) {
        return new ResourceLocation(Constants.MOD_ID, i);
    }

    private void generateAdvancements(Consumer<Advancement> consumer) {

        final Advancement root = Advancement.Builder.advancement()
                .addCriterion("root", new ImpossibleTrigger.TriggerInstance())
                .display(new DisplayBuilder()
                        .frame(FrameType.TASK)
                        .background(modLoc("textures/block/wall.png"))
                        .item(new ItemStack(Machines.MACHINE_BLOCK_ITEM_NORMAL.get()))
                        .id(Advancements.ROOT)
                        .toast(false).hidden(false).chat(false)
                        .build())
                .save(consumer, modLoc("root").toString());

        Advancement.Builder.advancement()
                .parent(root)
                .addCriterion("got_stuck", HowDidYouGetHereTrigger.Instance.create())
                .display(new DisplayBuilder()
                        .frame(FrameType.CHALLENGE)
                        .item(new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()))
                        .id(Advancements.HOW_DID_YOU_GET_HERE)
                        .toast(false).hidden(true)
                        .build())
                .save(consumer, Advancements.HOW_DID_YOU_GET_HERE.toString());

        Advancement.Builder.advancement()
                .parent(root)
                .addCriterion("recursion", BasicPlayerAdvTrigger.Instance.create(Advancements.RECURSIVE_ROOMS))
                .display(new DisplayBuilder()
                        .frame(FrameType.CHALLENGE)
                        .item(new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()))
                        .id(Advancements.RECURSIVE_ROOMS)
                        .toast(false).hidden(true)
                        .build())
                .save(consumer, Advancements.RECURSIVE_ROOMS.toString());

        Advancement.Builder.advancement()
                .parent(root)
                .addCriterion("obtained_wall", InventoryChangeTrigger.TriggerInstance.hasItems(Walls.BLOCK_BREAKABLE_WALL.get()))
                .display(new DisplayBuilder()
                        .frame(FrameType.TASK)
                        .item(new ItemStack(Walls.BLOCK_BREAKABLE_WALL.get()))
                        .id(Advancements.FOUNDATIONS)
                        .build())
                .save(consumer, Advancements.FOUNDATIONS.toString());

        final Advancement psd = Advancement.Builder.advancement()
                .parent(root)
                .addCriterion("obtained_psd", InventoryChangeTrigger.TriggerInstance.hasItems(Shrinking.PERSONAL_SHRINKING_DEVICE.get()))
                .display(new DisplayBuilder()
                        .frame(FrameType.TASK)
                        .item(new ItemStack(Shrinking.PERSONAL_SHRINKING_DEVICE.get()))
                        .id(Advancements.GOT_SHRINKING_DEVICE)
                        .build())
                .save(consumer, Advancements.GOT_SHRINKING_DEVICE.toString());

        machineAdvancement(consumer, psd, Advancements.CLAIMED_TINY_MACHINE, Machines.MACHINE_BLOCK_ITEM_TINY);
        machineAdvancement(consumer, psd, Advancements.CLAIMED_SMALL_MACHINE, Machines.MACHINE_BLOCK_ITEM_SMALL);
        machineAdvancement(consumer, psd, Advancements.CLAIMED_NORMAL_MACHINE, Machines.MACHINE_BLOCK_ITEM_NORMAL);
        machineAdvancement(consumer, psd, Advancements.CLAIMED_LARGE_MACHINE, Machines.MACHINE_BLOCK_ITEM_LARGE);
        machineAdvancement(consumer, psd, Advancements.CLAIMED_GIANT_MACHINE, Machines.MACHINE_BLOCK_ITEM_GIANT);
        machineAdvancement(consumer, psd, Advancements.CLAIMED_MAX_MACHINE, Machines.MACHINE_BLOCK_ITEM_MAXIMUM);
    }

    private void machineAdvancement(Consumer<Advancement> consumer, Advancement root, ResourceLocation advancement, Supplier<Item> item) {
        Advancement.Builder.advancement()
                .parent(root)
                .addCriterion("claimed_machine", BasicPlayerAdvTrigger.Instance.create(advancement))
                .display(new DisplayBuilder()
                        .frame(FrameType.TASK)
                        .item(new ItemStack(item.get()))
                        .id(advancement)
                        .build())
                .save(consumer, advancement.toString());
    }

    @Override
    public String getName() {
        return "CompactMachinesAdvancements";
    }

    private static class DisplayBuilder {

        private ItemStack stack;
        private ResourceLocation translationId;
        private boolean showToast = true;
        private boolean showInChat = true;
        private boolean isHidden = false;
        private ResourceLocation background = null;
        private FrameType frame = FrameType.TASK;

        public DisplayBuilder() {
        }

        public DisplayInfo build() {
            return new DisplayInfo(stack,
                    TranslationUtil.advancementTitle(translationId),
                    TranslationUtil.advancementDesc(translationId),
                    background,
                    frame,
                    showToast, showInChat, isHidden);
        }

        public DisplayBuilder item(ItemStack item) {
            stack = item;
            return this;
        }

        public DisplayBuilder id(ResourceLocation transId) {
            translationId = transId;
            return this;
        }

        public DisplayBuilder toast(boolean toast) {
            this.showToast = toast;
            return this;
        }

        public DisplayBuilder chat(boolean chat) {
            this.showInChat = chat;
            return this;
        }

        public DisplayBuilder hidden(boolean hidden) {
            this.isHidden = hidden;
            return this;
        }

        public DisplayBuilder background(ResourceLocation resource) {
            this.background = resource;
            return this;
        }

        public DisplayBuilder frame(FrameType frame) {
            this.frame = frame;
            return this;
        }
    }
}
