package dev.compactmods.machines.datagen.lang;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.room.RoomSize;
import net.minecraft.data.DataGenerator;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class EnglishLangGenerator extends BaseLangGenerator {
    public EnglishLangGenerator(DataGenerator gen) {
        super(gen, "en_us");
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();

        addMessage(Messages.CANNOT_ENTER_MACHINE, "You fumble with the shrinking device, to no avail. It refuses to work.");
        addMessage(Messages.NO_MACHINE_DATA, "No machine data loaded; report this.");
        addMessage(Messages.ROOM_SPAWNPOINT_SET, "New spawn point set.");
        addMessage(Messages.HOW_DID_YOU_GET_HERE, "How did you get here?!");
        addMessage(Messages.NEW_MACHINE, "New Machine");
        addMessage(Messages.TELEPORT_OUT_OF_BOUNDS, "An otherworldly force prevents your teleportation.");
        addMessage(Messages.NO_TUNNEL_SIDE, "There are no available sides for this tunnel type.");

        // 1 = Display Name, 2 = Chunk, 3 = Size
        addMessage(Messages.PLAYER_ROOM_INFO, "Player '%1$s' is inside a %3$s room at %2$s.");
        addMessage(Messages.MACHINE_ROOM_INFO, "Machine at %1$s is bound to a %2$s size room at %3$s");

        addCommand(CMCommands.NOT_IN_COMPACT_DIMENSION, "Cannot use that command outside of a machine room.");
        addCommand(CMCommands.FAILED_CMD_FILE_ERROR, "Failed to execute command; there was a file error. Check logs.");
        addCommand(CMCommands.MACHINE_NOT_BOUND, "Machine at %s is not bound to a room.");
        addCommand(CMCommands.ROOM_REG_COUNT, "Number of registered rooms: %s");
        addCommand(CMCommands.MACHINE_REG_DIM, "[%s]: %s");
        addCommand(CMCommands.MACHINE_REG_TOTAL, "Total: %s");
        addCommand(CMCommands.LEVEL_REGISTERED, "Compact Machine dimension found.");
        addCommand(CMCommands.LEVEL_NOT_FOUND, "Compact Machine dimension could not be found.");
        addCommand(CMCommands.ROOM_NOT_FOUND, "Room [%s] could not be found.");
        addCommand(CMCommands.SPAWN_CHANGED_SUCCESSFULLY, "Spawnpoint for room [%s] was changed successfully.");

        addAdvancementTranslations();

        add(Registration.BLOCK_BREAKABLE_WALL.get(), "Compact Machine Wall");
        add(Registration.BLOCK_SOLID_WALL.get(), "Solid Compact Machine Wall");
        add(Tunnels.BLOCK_TUNNEL_WALL.get(), "Solid Compact Machine Wall (with Tunnel)");

        add(Registration.PERSONAL_SHRINKING_DEVICE.get(), "Personal Shrinking Device");

        add(CompactMachines.MOD_ID + ".direction.side", "Side: %s");
        add(CompactMachines.MOD_ID + ".connected_block", "Connected: %s");

        addTunnel(Tunnels.ITEM_TUNNEL_DEF.get(), "Item Tunnel");
        addTunnel(Tunnels.FLUID_TUNNEL_DEF.get(), "Fluid Tunnel");
        addTunnel(Tunnels.FORGE_ENERGY.get(), "Energy Tunnel");
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
        addCommand(CMCommands.CANNOT_GIVE_MACHINE, "Failed to give a new machine to player.");
        addCommand(CMCommands.MACHINE_GIVEN, "Created a new machine item and gave it to %s.");

        addMessage(Messages.UNKNOWN_ROOM_CHUNK, "Unknown room at %s; please verify it exists.");

        add("itemGroup." + CompactMachines.MOD_ID, "Compact Machines");

        add("biome." + CompactMachines.MOD_ID + ".machine", "Compact Machine");

        add("compactmachines.psd.pages.machines.title", "Compact Machines");
        add("compactmachines.psd.pages.machines", "Compact Machines are the core mechanic of this mod. They allow you to build large " +
                "rooms in a single block space connected to the outside world. They come in various sizes ranging from 3x3x3 to 13x13x13.\n\n" +
                "You can use Tunnels to connect the outside block faces with any of the inside walls to transport items, fluids etc.\n\n" +
                "You can enter a Compact Machine by right-clicking it with a Personal Shrinking Device. Please use JEI to look up crafting recipes.");

        add("jei.compactmachines.machines", "Machines are used to make pocket dimensions. Craft a machine and place it in world, then use a Personal Shrinking Device to go inside.");
        add("jei.compactmachines.shrinking_device", "Use the Personal Shrinking Device (PSD) on a machine in order to enter a compact space. " +
                "You can also right click it in the overworld for more info.");
    }

    @Override
    protected String getSizeTranslation(RoomSize size) {
        return capitalize(size.getSerializedName());
    }
}
