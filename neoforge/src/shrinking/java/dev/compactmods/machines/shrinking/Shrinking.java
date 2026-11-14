package dev.compactmods.machines.shrinking;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.GameRulesHelper;
import dev.compactmods.machines.core.data.DataFileUtil;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import dev.compactmods.machines.shrinking.api.history.PlayerTeleportHistoryManager;
import dev.compactmods.machines.shrinking.api.capability.PlayerShrinkingHandler;
import dev.compactmods.machines.shrinking.api.history.RoomEntryMethod;
import dev.compactmods.machines.shrinking.capability.ServerPlayerShrinkingHandler;
import dev.compactmods.machines.shrinking.history.UsedShrinkingDeviceOnMachine;
import dev.compactmods.machines.shrinking.history.ServerPlayerTeleportHistoryManager;
import dev.compactmods.machines.shrinking.history.UsedTeleportCommand;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collections;
import java.util.function.Supplier;

public class Shrinking {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachinesCore.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);
    private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(BuiltInRegistries.GAME_RULE, CompactMachinesCore.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);
    private static final DeferredRegister<RoomEntryMethod.Type<?>> ROOM_ENTRYPOINT_TYPES = DeferredRegister.create(RoomEntryMethod.Type.REGISTRY_KEY, CompactMachinesCore.MOD_ID);


    public static final EntityCapability<PlayerShrinkingHandler, RoomInstance> SHRINK = EntityCapability.create(CompactMachinesCore.identifier("shrink"),
            PlayerShrinkingHandler.class, RoomInstance.class);

    public static final EntityCapability<PlayerTeleportHistoryManager, Void> HISTORY_MANAGER = EntityCapability.createVoid(
            CompactMachinesCore.identifier("shrinking_history"), PlayerTeleportHistoryManager.class);

    public static final Supplier<AttachmentType<String>> CURRENT_ROOM_CODE = ATTACHMENT_TYPES.register("current_room_code", () -> AttachmentType
            .builder(() -> "")
            .serialize(Codec.STRING.fieldOf("roomCode"))
            .build());

    public static void init(IEventBus modBus) {
        Items.prepare();
        DataComponents.prepare();
        GameRules.prepare();
        EntryMethods.prepare();;

        ROOM_ENTRYPOINT_TYPES.makeRegistry(b -> {
            b.sync(true);
        });

        PlayerEventHandler.registerEvents();

        registerContent(modBus);
    }

    // public static final DeferredItem<Item> RESIZING_MODULE = Registries.ITEMS.register("resizing_module", Registries::basicItem);

    public interface Items {

        DeferredItem<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = ITEMS.register("personal_shrinking_device",
                () -> new PersonalShrinkingDevice(new Item.Properties()
                        .setId(PersonalShrinkingDevice.RESOURCE_KEY)
                        .component(DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG)
                        .stacksTo(1)));

        DeferredItem<Item> SHRINKING_MODULE = ITEMS.registerItem("shrinking_module", Item::new);
        DeferredItem<Item> ENLARGING_MODULE = ITEMS.registerItem("enlarging_module", Item::new);

        static void prepare() {}
    }

    public interface GameRules {
        /// For hardcore-style packs. If a shrinking item is successfully used to LEAVE a room,
        /// it will also be damaged. Off by default.
        Identifier DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY = CompactMachinesCore.identifier("damage_psd_on_exit");

        DeferredHolder<GameRule<?>, GameRule<Boolean>> DAMAGE_PSD_ITEMS_ON_ROOM_EXIT = GAME_RULES
                .register(DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY.getPath(), () -> GameRulesHelper.makeBooleanRule(false));

        static void prepare() {
        }
    }

    public interface DataComponents {
        String KEY_SHRINKING_CONFIG = "shrinking_device";

        DeferredHolder<DataComponentType<?>, DataComponentType<ShrinkingDeviceConfiguration>> SHRINKING_CONFIG = DATA_COMPONENTS
                .registerComponentType(KEY_SHRINKING_CONFIG, (builder) -> builder
                        .persistent(ShrinkingDeviceConfiguration.CODEC)
                        .networkSynchronized(ShrinkingDeviceConfiguration.STREAM_CODEC));

        static void prepare() {
        }
    }

    public interface EntryMethods {

        Holder<RoomEntryMethod.Type<?>> PERSONAL_SHRINKING_DEVICE = ROOM_ENTRYPOINT_TYPES
                .register("shrinking_device", RoomEntryMethod.simple(UsedShrinkingDeviceOnMachine.MAP_CODEC));

        Holder<RoomEntryMethod.Type<?>> TELEPORT_COMMAND = ROOM_ENTRYPOINT_TYPES.register("teleport_command",
                RoomEntryMethod.simple(UsedTeleportCommand.MAP_CODEC));
//
//        Holder<RoomEntryMethod> UNKNOWN = ROOM_ENTRY_METHODS.register("unknown",
//                i -> RoomEntryMethod.simple(i, TP_PRECISE));

        static void prepare() {
        }
    }

    public static void registerContent(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
        ITEMS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        GAME_RULES.register(modBus);
        ROOM_ENTRYPOINT_TYPES.register(modBus);

        modBus.addListener((RegisterCapabilitiesEvent caps) -> {
            caps.registerEntity(Shrinking.SHRINK, EntityTypes.PLAYER, (player, roomInstance) -> {
                if (player instanceof ServerPlayer serverPlayer)
                    return new ServerPlayerShrinkingHandler(serverPlayer, roomInstance);

                return null;
            });

            caps.registerEntity(HISTORY_MANAGER, EntityTypes.PLAYER, (player, _) -> {
                final var server = player.level().getServer();
                return new ServerPlayerTeleportHistoryManager(server, player.getUUID(), 5, Collections.emptyList());
            });
        });

        modBus.addListener((BuildCreativeModeTabContentsEvent addToTabs) -> {
            if (addToTabs.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                addToTabs.accept(Items.PERSONAL_SHRINKING_DEVICE.get());
            }
        });
    }
}

