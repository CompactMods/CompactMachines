package dev.compactmods.machines.machine;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.machine.block.CompactMachineBlock;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface Machines {

    // :machines owns its own DeferredRegisters (mirrors :shrinking / :room-system).
    // They are bound to the mod bus by init(...) below, invoked from the mod entrypoint.
    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachinesCore.MOD_ID);
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactMachinesCore.MOD_ID);
    DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, CompactMachinesCore.MOD_ID);
    DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);
    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachinesCore.MOD_ID);

    UnaryOperator<BlockBehaviour.Properties> MACHINE_BLOCK_PROPS = props -> props
            .instrument(NoteBlockInstrument.COW_BELL)
            .pushReaction(PushReaction.IMMOVEABLE)
            .sound(SoundType.METAL)
            .strength(8.0F, 20.0F)
            .noOcclusion()
            .requiresCorrectToolForDrops();

    Supplier<Item.Properties> MACHINE_ITEM_PROPS = Item.Properties::new;

    interface Blocks {
        ResourceKey<Block> MACHINE_KEY = ResourceKey.create(Registries.BLOCK, CompactMachinesCore.identifier("machine"));

        DeferredBlock<CompactMachineBlock> MACHINE = BLOCKS.registerBlock("machine", props ->
                new CompactMachineBlock(MACHINE_BLOCK_PROPS.apply(props).setId(MACHINE_KEY)));

        static void prepare() {
        }
    }

    interface Items {
        DeferredItem<BoundCompactMachineItem> MACHINE = ITEMS.register("machine",
                () -> new BoundCompactMachineItem(MACHINE_ITEM_PROPS.get()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachinesCore.identifier("machine")))
                        .overrideDescription(BoundCompactMachineItem.FALLBACK_ID)));

        static void prepare() {
        }

        static ItemStack boundToRoom(String roomCode) {
            ItemStack stack = new ItemStack(net.minecraft.world.item.Items.PAPER);
            stack.set(Rooms.DataComponents.BOUND_ROOM_CODE, roomCode);
            return stack;
        }

        static ItemStack forNewRoom(Holder.Reference<RoomTemplate> templateHolder) {
            return templateForNewRoom(templateHolder).create();
        }

        /**
         * Same as {@link #forNewRoom}, but safe to build before item components are bound
         * (e.g. while datagen bootstraps recipe registries).
         */
        static ItemStackTemplate templateForNewRoom(Holder.Reference<RoomTemplate> templateHolder) {
            var template = templateHolder.value();

            final var components = DataComponentPatch.builder()
                    .set(Rooms.DataComponents.ROOM_TEMPLATE_ID.get(), templateHolder.key().identifier())
                    .set(DataComponents.MACHINE_COLOR.get(), template.defaultMachineColor())
                    .build();

            return new ItemStackTemplate(net.minecraft.world.item.Items.PAPER, components);
        }
    }

    interface BlockEntities {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<CompactMachineBlockEntity>> MACHINE = BLOCK_ENTITIES
                .register(MachineConstants.MACHINE_IDENTIFIER.getPath(),
                        () -> new BlockEntityType<>(CompactMachineBlockEntity::new, Blocks.MACHINE.get()));

        static void prepare() {
        }
    }

    interface DataComponents {
        DeferredHolder<DataComponentType<?>, DataComponentType<MachineColor>> MACHINE_COLOR = DATA_COMPONENTS
                .registerComponentType("machine_color", (builder) -> builder
                        .persistent(MachineColor.CODEC)
                        .networkSynchronized(MachineColor.STREAM_CODEC));

        DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> PRIDE_FLAG = DATA_COMPONENTS
                .registerComponentType("pride_flag", (builder) -> builder
                        .persistent(Identifier.CODEC)
                        .networkSynchronized(Identifier.STREAM_CODEC));

        static void prepare() {
        }
    }

    interface Attachments {
        Supplier<AttachmentType<GlobalPos>> OPEN_MACHINE_POS = ATTACHMENT_TYPES.register("open_machine", () -> AttachmentType
                .builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
                .serialize(GlobalPos.MAP_CODEC, Predicates.alwaysFalse())
                .build());

        static void prepare() {
        }
    }

    DeferredHolder<MenuType<?>, MenuType<MachineUIMenu>> MACHINE_UI_MENU = MENUS.register("machine_ui",
            () -> IMenuTypeExtension.create(MachineUIMenu::new));

    static void prepare() {
        Blocks.prepare();
        Items.prepare();
        BlockEntities.prepare();
        DataComponents.prepare();
        Attachments.prepare();
    }

    static void init(IEventBus modBus) {
        prepare();
        registerContent(modBus);
        registerEvents(modBus);
    }

    static void registerContent(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        ATTACHMENT_TYPES.register(modBus);
    }

    static void registerEvents(IEventBus modBus) {

    }
}
