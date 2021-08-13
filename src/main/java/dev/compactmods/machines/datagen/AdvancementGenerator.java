package dev.compactmods.machines.datagen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.trigger.HowDidYouGetHereTrigger;
import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class AdvancementGenerator implements IDataProvider {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;

    public AdvancementGenerator(DataGenerator gen) {
        this.generator = gen;
    }

    @Override
    public void run(DirectoryCache cache) {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (adv) -> {
            if (!set.add(adv.getId())) {
                throw new IllegalStateException("Duplicate advancement " + adv.getId());
            } else {
                Path path1 = path.resolve("data/" + adv.getId().getNamespace() + "/advancements/" + adv.getId().getPath() + ".json");

                try {
                    IDataProvider.save(GSON, cache, adv.deconstruct().serializeToJson(), path1);
                } catch (IOException ioexception) {
                    CompactMachines.LOGGER.error("Couldn't save advancement {}", path1, ioexception);
                }

            }
        };

        generateAdvancements(consumer);
    }

    private ResourceLocation modLoc(String i) {
        return new ResourceLocation(CompactMachines.MOD_ID, i);
    }

    private void generateAdvancements(Consumer<Advancement> consumer) {

        final Advancement root = Advancement.Builder.advancement()
                .addCriterion("root", new ImpossibleTrigger.Instance())
                .display(new DisplayBuilder()
                        .frame(FrameType.TASK)
                        .background(modLoc("textures/block/wall.png"))
                        .item(new ItemStack(Registration.MACHINE_BLOCK_ITEM_NORMAL.get()))
                        .id(Advancements.ROOT)
                        .toast(false).hidden(false).chat(false)
                        .build())
                .save(consumer, modLoc("root").toString());

        Advancement.Builder.advancement()
                .parent(root)
                .addCriterion("got_stuck", HowDidYouGetHereTrigger.Instance.create())
                .display(new DisplayBuilder()
                        .frame(FrameType.CHALLENGE)
                        .item(new ItemStack(Registration.PERSONAL_SHRINKING_DEVICE.get()))
                        .id(Advancements.HOW_DID_YOU_GET_HERE)
                        .toast(false).hidden(true)
                        .build())
                .save(consumer, Advancements.HOW_DID_YOU_GET_HERE.toString());
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
