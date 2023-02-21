package dev.compactmods.machines.data.generated.lang;

import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.dimension.VoidAirBlock;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.BuiltInTunnels;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.upgrade.BuiltInUpgrades;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.data.DataGenerator;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class EnglishLangGenerator extends BaseLangGenerator {
    public EnglishLangGenerator(DataGenerator gen) {
        super(gen, "en_us");
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();

        final var machineTranslation = getMachineTranslation();
        add("machine.compactmachines.tiny", "%s (%s)".formatted(machineTranslation, "Tiny"));
        add("machine.compactmachines.small", "%s (%s)".formatted(machineTranslation, "Small"));
        add("machine.compactmachines.normal", "%s (%s)".formatted(machineTranslation, "Normal"));
        add("machine.compactmachines.large", "%s (%s)".formatted(machineTranslation, "Large"));
        add("machine.compactmachines.giant", "%s (%s)".formatted(machineTranslation, "Giant"));
        add("machine.compactmachines.colossal", "%s (%s)".formatted(machineTranslation, "Colossal"));
        add("machine.compactmachines.absurd", "%s (%s)".formatted(machineTranslation, "Absurd"));

        addMessage(Messages.CANNOT_ENTER_MACHINE, "You fumble with the shrinking device, to no avail. It refuses to work.");
        addMessage(Messages.NO_MACHINE_DATA, "No machine data loaded; report this.");
        addMessage(Messages.ROOM_SPAWNPOINT_SET, "New spawn point set.");
        addMessage(Messages.HOW_DID_YOU_GET_HERE, "How did you get here?!");
        addMessage(Messages.NEW_MACHINE, "New Machine");
        addMessage(Messages.TELEPORT_OUT_OF_BOUNDS, "An otherworldly force prevents your teleportation.");
        addMessage(Messages.NO_TUNNEL_SIDE, "There are no available sides for this tunnel type.");

        addMessage(Messages.NOT_ROOM_OWNER, "You are not the room owner; only %s may make changes.");

        // 1 = Display Name, 2 = Chunk, 3 = Size
        addMessage(Messages.PLAYER_ROOM_INFO, "Player '%1$s' is inside a %3$s room at %2$s.");
        addMessage(Messages.MACHINE_ROOM_INFO, "Machine at %1$s is bound to a %2$s size room at %3$s");

        addMessage(Messages.CANNOT_RENAME_NOT_OWNER, "Only %s may rename this room.");

        addCommand(CMCommands.NOT_IN_COMPACT_DIMENSION, "Cannot use that command outside of a machine room.");
        addCommand(CMCommands.FAILED_CMD_FILE_ERROR, "Failed to execute command; there was a file error. Check logs.");
        addCommand(CMCommands.MACHINE_NOT_BOUND, "Machine at %s is not bound to a room.");
        addCommand(CMCommands.ROOM_REG_COUNT, "Number of registered rooms: %s");
        addCommand(CMCommands.MACHINE_REG_DIM, "[%s]: %s");
        addCommand(CMCommands.MACHINE_REG_TOTAL, "Total: %s");
        addCommand(CMCommands.LEVEL_REGISTERED, "Compact Machine dimension found.");
        addCommand(CMCommands.LEVEL_NOT_FOUND, "Compact Machine dimension could not be found.");
        addCommand(CMCommands.ROOM_NOT_FOUND, "Room [%s] could not be found.");
        addCommand(CMCommands.SPAWN_CHANGED_SUCCESSFULLY, "Spawn point for room [%s] was changed successfully.");

        addAdvancementTranslations();

        addBlock(Walls.BLOCK_BREAKABLE_WALL, "Compact Machine Wall");
        addBlock(Walls.BLOCK_SOLID_WALL, "Solid Compact Machine Wall");
        addBlock(Tunnels.BLOCK_TUNNEL_WALL, "Solid Compact Machine Wall (with Tunnel)");

        add(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), "Personal Shrinking Device");

        add(Constants.MOD_ID + ".direction.side", "Side: %s");
        add(Constants.MOD_ID + ".connected_block", "Connected: %s");

        addTunnel(BuiltInTunnels.ITEM_TUNNEL_DEF, "Item Tunnel");
        addTunnel(BuiltInTunnels.FLUID_TUNNEL_DEF, "Fluid Tunnel");
        addTunnel(BuiltInTunnels.FORGE_ENERGY, "Energy Tunnel");
        // addTunnel(Tunnels.REDSTONE_IN_DEF.get(), "Redstone Tunnel (In)");
        // addTunnel(Tunnels.REDSTONE_OUT_DEF.get(), "Redstone Tunnel (Out)");

        addTooltip(Tooltips.Details.PERSONAL_SHRINKING_DEVICE, "Used as in-game documentation and to enter Compact Machines.");
        addTooltip(Tooltips.Details.SOLID_WALL, "Warning! Unbreakable for non-creative players!");

        addTooltip(Tooltips.HINT_HOLD_SHIFT, "Hold shift for details.");
        addTooltip(Tooltips.UNKNOWN_PLAYER_NAME, "Unknown Player");

        addTooltip(Tooltips.Machines.ID, "Machine #%s");
        addTooltip(Tooltips.Machines.OWNER, "Owner: %s");
        addTooltip(Tooltips.Machines.SIZE, "Internal Size: %1$sx%1$sx%1$s");
        addTooltip(Tooltips.Machines.BOUND_TO, "Bound to: %1$s");

        addTooltip(Tooltips.TUNNEL_TYPE, "Type ID: %1$s");
        addTooltip(Tooltips.UNKNOWN_TUNNEL_TYPE, "Unknown Tunnel Type (%s)");

        addTooltip(Tooltips.ROOM_NAME, "Bound to room: %s");

        //region Upgrades
        add(BuiltInUpgrades.CHUNKLOAD, "Chunkloader Upgrade");

        addMessage(Messages.ALREADY_HAS_UPGRADE, "Upgrade has already been applied to room.");
        addMessage(Messages.UPGRADE_NOT_PRESENT, "Upgrade is not applied to the room.");

        addMessage(Messages.UPGRADE_APPLIED, "Upgrade applied to room.");
        addMessage(Messages.UPGRADE_ADD_FAILED, "Upgrade failed to apply to room.");

        addMessage(Messages.UPGRADE_REMOVED, "Upgrade removed from room.");
        addMessage(Messages.UPGRADE_REM_FAILED, "Upgrade removal failed to apply to room.");

        addTooltip(Tooltips.ROOM_UPGRADE_TYPE, "Type: %s");
        addTooltip(Tooltips.TUTORIAL_APPLY_ROOM_UPGRADE, "Use on a bound machine block to apply upgrade.");
        //endregion

        addCommand(CMCommands.CANNOT_GIVE_MACHINE, "Failed to give a new machine to player.");
        addCommand(CMCommands.MACHINE_GIVEN, "Created a new machine item and gave it to %s.");

        addMessage(Messages.UNKNOWN_ROOM_CHUNK, "Unknown room at %s; please verify it exists.");

        add("itemGroup." + Constants.MOD_ID, "Compact Machines");

        add("biome." + Constants.MOD_ID + ".machine", "Compact Machine");

        add("compactmachines.psd.pages.machines.title", "Compact Machines");
        add("compactmachines.psd.pages.machines", "Compact Machines are the core mechanic of this mod. They allow you to build large " +
                "rooms in a single block space connected to the outside world. They come in various sizes ranging from 3x3x3 to 13x13x13.\n\n" +
                "You can use Tunnels to connect the outside block faces with any of the inside walls to transport items, fluids etc.\n\n" +
                "You can enter a Compact Machine by right-clicking it with a Personal Shrinking Device. Please use JEI to look up crafting recipes.");

        add("jei.compactmachines.machines", "Machines are used to make pocket dimensions. Craft a machine and place it in world, then use a Personal Shrinking Device to go inside.");
        add("jei.compactmachines.shrinking_device", "Use the Personal Shrinking Device (PSD) on a machine in order to enter a compact space. " +
                "You can also right click it in the overworld for more info.");
        add("death.attack." + VoidAirBlock.DAMAGE_SOURCE.msgId, "%1$s failed to enter the void");

        add("curios.identifier.psd", "Personal Shrinking Device");

        add(MachineRoomUpgrades.WORKBENCH_BLOCK.get(), "Workbench");
        add("entity.minecraft.villager.compactmachines.tinkerer", "Tinkerer");
    }

    @Override
    protected String getSizeTranslation(RoomSize size) {
        return capitalize(size.getSerializedName());
    }
}
