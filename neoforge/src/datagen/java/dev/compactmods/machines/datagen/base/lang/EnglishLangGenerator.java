package dev.compactmods.machines.datagen.base.lang;


import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.room.client.RoomKeyMappings;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.core.Translations;
import dev.compactmods.machines.Advancements;
import dev.compactmods.machines.room.command.CommandTranslations;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeNode;
import dev.compactmods.machines.datagen.debug.DebugNodesDatapack;
import dev.compactmods.machines.machine.i18n.MachineTranslations;
import dev.compactmods.machines.client.keybinds.room.RoomExitKeyMapping;
import dev.compactmods.machines.client.creative.CreativeTabs;
import dev.compactmods.machines.room.RoomTranslations;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelTranslations;
import dev.compactmods.machines.upgrades.storage.nodes.StorageNodes;
import dev.compactmods.machines.machine.config.MachineSideNode;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelTypes;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;

import java.util.List;

public class EnglishLangGenerator extends dev.compactmods.machines.datagen.base.lang.BaseLangGenerator {
    public EnglishLangGenerator(PackOutput packOutput) {
        super(packOutput, "en_us");
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();

        blocksAndItems();

        add(Translations.IDs.HOW_DID_YOU_GET_HERE, "How did you get here?!");
        add(Translations.IDs.TELEPORT_OUT_OF_BOUNDS, "An otherworldly force prevents your teleportation.");

        // Machine Translations
        add(MachineTranslations.IDs.OWNER, "Owner: %s");
        add(MachineTranslations.IDs.SIZE, "Internal Size: %1$s");
        add(MachineTranslations.IDs.BOUND_TO, "Bound to: %1$s");
        add(MachineTranslations.IDs.NEW_MACHINE, "New Machine");

        // Room Translations
        add(RoomTranslations.IDs.ROOM_SPAWNPOINT_SET, "New spawn point set.");
        add(RoomTranslations.IDs.MACHINE_ROOM_INFO, "Machine at %1$s is bound to a %2$s size room at %3$s");
        add(RoomTranslations.IDs.PLAYER_ROOM_INFO, "Player '%1$s' is inside room %2$s.");

        // Room Errors
        add(RoomTranslations.IDs.Errors.CANNOT_ENTER_ROOM, "You fumble with the shrinking device, to no avail. It refuses to work.");
        add(RoomTranslations.IDs.Errors.UNKNOWN_ROOM_BY_CODE, "Room [%s] could not be found.");

        // Room Templates
        add(RoomTemplate.I18N_STRUCTURE_GEN_TOOLTIP, "Generates %s structures upon room creation.");
        add(RoomTemplate.I18N_INTERNAL_ROOM_DIMS, "Internal Size: %s");


        commands();
        advancements();
        tunnels();
        storageNodes();

        add(CompactMachinesCore.MOD_ID + ".direction.side", "Side: %s");
        add(CompactMachinesCore.MOD_ID + ".connected_block", "Connected: %s");

        add(Translations.IDs.HINT_HOLD_SHIFT, "Hold shift for details.");

        addCreativeTab(CreativeTabs.MAIN_RL, "Compact Machines");

        addBiome(CompactDimension.COMPACT_BIOME_KEY, "Compact Machine");
        addDimension(CompactDimension.LEVEL_KEY, "Compact Machine");

        add("jei.CompactMachinesCore.machines", "Machines are used to make pocket dimensions. Craft a machine and place it in world, then use a Personal Shrinking Device to go inside.");
        add("jei.CompactMachinesCore.shrinking_device", "Use the Personal Shrinking Device (PSD) on a machine in order to enter a compact space.");
        // add("death.attack." + VoidAirBlock.DAMAGE_SOURCE.msgId, "%1$s failed to enter the void");

        add("curios.identifier.psd", "Personal Shrinking Device");

        add(Villagers.SPATIAL_WORKBENCH.get(), "Spatial Workbench");
        addVillagerProfession(Villagers.TINKERER, "Spatial Tinkerer");

        add(RoomKeyMappings.CATEGORY.id().toLanguageKey(), "Compact Machines");
        add(RoomExitKeyMapping.I18n.NAME, "Quick-Exit Compact Machine");

        addJade();

        add(TreeCutterUpgradeNode.KEY.identifier().toLanguageKey("node"), "Tree Cutter Upgrade");

        // Debug nodes (dev-only datapack)
        add(DebugNodesDatapack.INFINITE_ITEMS.identifier().toLanguageKey("node"), "Infinite Items");
        add(DebugNodesDatapack.INFINITE_ITEMS.identifier().toLanguageKey("node", "item"), "Item");
        add(DebugNodesDatapack.INFINITE_ITEMS.identifier().toLanguageKey("node", "storage"), "Storage");
        add(DebugNodesDatapack.VOID_ITEMS.identifier().toLanguageKey("node"), "Void");
        add(DebugNodesDatapack.VOID_ITEMS.identifier().toLanguageKey("node", "storage"), "Storage");
        add(TreeCutterUpgradeNode.KEY.identifier().toLanguageKey("node", TreeCutterUpgradeNode.ENABLED.name()), "Enabled");
        add(TreeCutterUpgradeNode.KEY.identifier().toLanguageKey("node", TreeCutterUpgradeNode.TOOLS.name()), "Tools");
        add(TreeCutterUpgradeNode.KEY.identifier().toLanguageKey("node", TreeCutterUpgradeNode.DROPS.name()), "Drops");

        addGamerule(CMGameRules.ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY, "Allow Survival OOB", "Allow out-of-bounds survival players");
        addGamerule(CMGameRules.ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY, "Allow Creative OOB", "Allow out-of-bounds creative players");
        addGamerule(CMGameRules.ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY, "Allow Spectators OOB", "Allow out-of-bounds spectators");
        addGamerule(CMGameRules.DAMAGE_OOB_PLAYERS_KEY, "Damage OOB Players", "Damage players that are out of bounds");

    }

    private void blocksAndItems() {
        final var machineTranslation = getMachineTranslation();

        addBlock(Machines.Blocks.MACHINE, machineTranslation);
        addBlock(Rooms.Blocks.BREAKABLE_WALL, "Compact Machine Wall");
        addBlock(Rooms.Blocks.SOLID_WALL, "Solid Compact Machine Wall");

        addItem(Rooms.Items.BREAKABLE_WALL, "Compact Machine Wall");
        addItem(Rooms.Items.SOLID_WALL, "Solid Compact Machine Wall");

        addItem(Shrinking.Items.PERSONAL_SHRINKING_DEVICE, "Personal Shrinking Device");
        addItem(Shrinking.Items.SHRINKING_MODULE, "Atom Shrinking Module");
        addItem(Shrinking.Items.ENLARGING_MODULE, "Atom Enlarging Module");
        add(Util.makeDescriptionId("block", CompactMachinesCore.identifier("bound_machine_fallback")), machineTranslation);
    }

    protected void advancements() {
        advancement(Advancements.ROOT, "Compact Machines", "");
        advancement(Advancements.FOUNDATIONS, "Foundations", "Obtain a breakable wall block.");
        advancement(Advancements.GOT_SHRINKING_DEVICE, "Personal Shrinking Device", "Obtain a Personal Shrinking Device");
        advancement(Advancements.HOW_DID_YOU_GET_HERE, "How Did You Get Here?!", "Which machine is the player in?!");
        advancement(Advancements.RECURSIVE_ROOMS, "Recursive Rooms", "To understand recursion, you must first understand recursion.");
    }

    private void storageNodes() {
        add(StorageNodes.NAMED_ITEM_STORAGE.identifier().toLanguageKey("node"), "Named Item Storage");
        add(StorageNodes.NAMED_FLUID_STORAGE.identifier().toLanguageKey("node"), "Named Fluid Storage");
        for (var node : List.of(StorageNodes.NAMED_ITEM_STORAGE, StorageNodes.NAMED_FLUID_STORAGE)) {
            add(node.identifier().toLanguageKey("node", StorageNodes.STORAGE.name()), "Storage");
            add(node.identifier().toLanguageKey("node", StorageNodes.HANDLER), "Handler");
        }

        add(MachineSideNode.KEY.identifier().toLanguageKey("node"), "Machine Sides");
        add(MachineSideNode.KEY.identifier().toLanguageKey("node", MachineSideNode.RESOURCE_TYPE), "Resource");
        for (var side : Direction.values()) {
            final var name = MachineSideNode.input(side);
            add(MachineSideNode.KEY.identifier().toLanguageKey("node", name), Character.toUpperCase(name.charAt(0)) + name.substring(1));
        }

        add(StorageNodes.NAMED_STORAGE_TYPE.getId().toLanguageKey("graph_type"), "Named Storage");
        add(StorageNodes.RESOURCE_TYPE.getId().toLanguageKey("graph_type"), "Resource Type");
        add(CompactMachinesCore.dotPrefix("machine_config"), "Machine Configuration");
    }

    private void tunnels() {
        addBlock(Tunnels.Blocks.TUNNEL_WALL, "Tunnel Wall");
        addItem(Tunnels.Items.TUNNEL, "Tunnel");

        add(TunnelType.translationKey(TunnelTypes.ITEM.identifier()), "Items");
        add(TunnelType.translationKey(TunnelTypes.FLUID.identifier()), "Fluids");

        add(TunnelTranslations.IDs.SCREEN_TITLE, "Tunnel");
        add(TunnelTranslations.IDs.NAME, "Storage name");
        add(TunnelTranslations.IDs.CREATE, "Create");
        add(TunnelTranslations.IDs.SAVE, "Save");
        add(TunnelTranslations.IDs.CAPACITY, "Capacity: %s");
        add(TunnelTranslations.IDs.CONTAINER_HINT, "Add %s to increase capacity");

        add(TunnelTranslations.IDs.Preview.CONNECTED, "Connected machines:");
        add(TunnelTranslations.IDs.Preview.NO_CONNECTIONS, "No connected machines");
        add(TunnelTranslations.IDs.Preview.MACHINE, "%s, %s, %s (%s): %s");

        add(TunnelTranslations.IDs.Errors.EMPTY_NAME, "A storage needs a name.");
        add(TunnelTranslations.IDs.Errors.NAME_TOO_LONG, "Names can be at most %s characters.");
        add(TunnelTranslations.IDs.Errors.DUPLICATE_NAME, "This room already has a storage named '%s'.");
        add(TunnelTranslations.IDs.Errors.MISSING_CONTAINER, "Put at least one %s in the slot.");
        add(TunnelTranslations.IDs.Errors.MISSING_TUNNEL, "You need a tunnel in hand.");
        add(TunnelTranslations.IDs.Errors.NO_TYPE, "Pick a tunnel type.");
        add(TunnelTranslations.IDs.Errors.UNKNOWN_TYPE, "That tunnel type isn't available.");
        add(TunnelTranslations.IDs.Errors.UNKNOWN_STORAGE, "No storage named '%s'.");
        add(TunnelTranslations.IDs.Errors.WALL_CHANGED, "The wall changed; try again.");
    }

    private void commands() {
        add(CommandTranslations.IDs.CANNOT_GIVE_MACHINE, "Failed to give a new machine to player.");
        add(CommandTranslations.IDs.MACHINE_GIVEN, "Created a new machine item and gave it to %s.");
        add(CommandTranslations.IDs.ROOM_COUNT, "Number of registered rooms: %s");
        add(CommandTranslations.IDs.SPAWN_CHANGED_SUCCESSFULLY, "Spawn point for room [%s] was changed successfully.");
    }

    private void addJade() {
        add("config.jade.plugin_CompactMachinesCore.bound_machine", "Bound Compact Machines");
        add("config.jade.plugin_CompactMachinesCore.show_owner", "Show Machine Owners");
    }
}
