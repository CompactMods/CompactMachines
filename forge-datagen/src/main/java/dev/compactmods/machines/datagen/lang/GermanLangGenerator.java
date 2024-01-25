package dev.compactmods.machines.datagen.lang;

import dev.compactmods.machines.forge.client.RoomExitKeyMapping;
import dev.compactmods.machines.forge.dimension.VoidAirBlock;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.forge.wall.Walls;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.forge.shrinking.Shrinking;
import dev.compactmods.machines.forgebuiltin.tunnel.BuiltInTunnels;
import dev.compactmods.machines.forgebuiltin.upgrade.BuiltInUpgrades;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class EnglishLangGenerator extends BaseLangGenerator {
    public EnglishLangGenerator(DataGenerator gen) {
        super(gen, "de_de");
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();

        final var machineTranslation = getMachineTranslation();
        add("machine.compactmachines.tiny", "%s (%s)".formatted(machineTranslation, "Winzig"));
        add("machine.compactmachines.small", "%s (%s)".formatted(machineTranslation, "Klein"));
        add("machine.compactmachines.normal", "%s (%s)".formatted(machineTranslation, "Normal"));
        add("machine.compactmachines.large", "%s (%s)".formatted(machineTranslation, "Groß"));
        add("machine.compactmachines.giant", "%s (%s)".formatted(machineTranslation, "Gigantisch"));
        add("machine.compactmachines.colossal", "%s (%s)".formatted(machineTranslation, "Kolossal"));
        add("machine.compactmachines.absurd", "%s (%s)".formatted(machineTranslation, "Absurd"));

        addMessage(Messages.CANNOT_ENTER_MACHINE, "Sie fummeln vergeblich am Schrumpfgerät herum. Es weigert sich zu arbeiten.");
        addMessage(Messages.NO_MACHINE_DATA, "Keine Maschinendaten geladen; melde dies.");
        addMessage(Messages.ROOM_SPAWNPOINT_SET, "Neuer Spawnpunkt gesetzt.");
        addMessage(Messages.HOW_DID_YOU_GET_HERE, "Wie bist du hier her gekommen?!");
        addMessage(Messages.NEW_MACHINE, "Neue Maschine");
        addMessage(Messages.TELEPORT_OUT_OF_BOUNDS, "Eine jenseitige Kraft verhindert Ihre Teleportation.");
        addMessage(Messages.NO_TUNNEL_SIDE, "Für diesen Tunneltyp sind keine Seiten verfügbar.");

        addMessage(Messages.NOT_ROOM_OWNER, "Sie sind nicht der Zimmereigentümer; Nur %s darf Änderungen vornehmen.");

        // 1 = Display Name, 2 = Chunk, 3 = Size
        addMessage(Messages.PLAYER_ROOM_INFO, "Spieler '%1$s' befindet sich in einem %3$s-Raum bei %2$s.");
        addMessage(Messages.MACHINE_ROOM_INFO, "Die Maschine bei %1$s ist an einen Raum der Größe %2$s bei %3$s gebunden");

        addMessage(Messages.CANNOT_RENAME_NOT_OWNER, "Nur %s darf diesen Raum umbenennen.");

        addCommand(CMCommands.NOT_IN_COMPACT_DIMENSION, "Dieser Befehl kann nicht außerhalb eines Maschinenraums verwendet werden.");
        addCommand(CMCommands.FAILED_CMD_FILE_ERROR, "Befehl konnte nicht ausgeführt werden; Es ist ein Dateifehler aufgetreten. Bericht prüfen.");
        addCommand(CMCommands.MACHINE_NOT_BOUND, "Maschine bei %s ist nicht an einen Raum gebunden.");
        addCommand(CMCommands.ROOM_REG_COUNT, "Anzahl der registrierten Räume: %s");
        addCommand(CMCommands.MACHINE_REG_DIM, "[%s]: %s");
        addCommand(CMCommands.MACHINE_REG_TOTAL, "Insgesamt: %s");
        addCommand(CMCommands.LEVEL_REGISTERED, "Compact Machine Dimension gefunden.");
        addCommand(CMCommands.LEVEL_NOT_FOUND, "Compact Machine Dimension konnte nicht gefunden werden.");
        addCommand(CMCommands.ROOM_NOT_FOUND, "Raum [%s] konnte nicht gefunden werden.");
        addCommand(CMCommands.SPAWN_CHANGED_SUCCESSFULLY, "Spawnpunkt für Raum [%s] erfolgreich geändert.");

        addAdvancementTranslations();

        addBlock(Walls.BLOCK_BREAKABLE_WALL, "Kompakte Maschinenwand");
        addBlock(Walls.BLOCK_SOLID_WALL, "Solide kompakte Maschinenwand");
        addBlock(Tunnels.BLOCK_TUNNEL_WALL, "Solide kompakte Maschinenwand (mit Tunnel)");
        add(Util.makeDescriptionId("block", new ResourceLocation(Constants.MOD_ID, "bound_machine_fallback")), "Gebundene Kompaktmaschine");

        add(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), "Persönliches Schrumpfgerät");

        add(Constants.MOD_ID + ".direction.side", "Seite: %s");
        add(Constants.MOD_ID + ".connected_block", "Verbunden: %s");

        addTunnel(BuiltInTunnels.ITEM_TUNNEL_DEF, "Itemtunnel");
        addTunnel(BuiltInTunnels.FLUID_TUNNEL_DEF, "Flüssigkeitstunnel");
        addTunnel(BuiltInTunnels.FORGE_ENERGY, "Energietunnel");
        // addTunnel(Tunnels.REDSTONE_IN_DEF.get(), "Redstone Tunnel (In)");
        // addTunnel(Tunnels.REDSTONE_OUT_DEF.get(), "Redstone Tunnel (Out)");

        addTooltip(Tooltips.Details.PERSONAL_SHRINKING_DEVICE, "Wird als In-Game-Dokumentation und zum Betreten von Compact Machines verwendet.");
        addTooltip(Tooltips.Details.SOLID_WALL, "Warnung! Unzerbrechlich für nicht-kreative Spieler!");

        addTooltip(Tooltips.CRAFT_TO_UPGRADE, "Craft, um auf eine neue Maschine aufzurüsten.");
        addTooltip(Tooltips.HINT_HOLD_SHIFT, "Halten Sie die Umschalttaste gedrückt, um Details anzuzeigen.");
        addTooltip(Tooltips.UNKNOWN_PLAYER_NAME, "Unbekannter Spieler");

        addTooltip(Tooltips.Machines.ID, "Maschine #%s");
        addTooltip(Tooltips.Machines.OWNER, "Besitzer: %s");
        addTooltip(Tooltips.Machines.SIZE, "Interne Größe: %1$sx%1$sx%1$s");
        addTooltip(Tooltips.Machines.BOUND_TO, "Gebunden zu: %1$s");

        addTooltip(Tooltips.TUNNEL_TYPE, "Typ ID: %1$s");
        addTooltip(Tooltips.UNKNOWN_TUNNEL_TYPE, "Unbekannter Tunneltyp (%s)");

        addTooltip(Tooltips.ROOM_NAME, "An den Raum gebunden: %s");

        addTooltip(Tooltips.NOT_YET_IMPLEMENTED, "Noch nicht implementiert");

        //region Upgrades
        add(BuiltInUpgrades.CHUNKLOAD, "Chunkloader-Upgrade");

        addMessage(Messages.ALREADY_HAS_UPGRADE, "Das Upgrade wurde bereits auf den Raum angewendet.");
        addMessage(Messages.UPGRADE_NOT_PRESENT, "Das Upgrade wurde nicht auf den Raum angewendet.");

        addMessage(Messages.UPGRADE_APPLIED, "Upgrade auf Raum angewendet.");
        addMessage(Messages.UPGRADE_ADD_FAILED, "Das Upgrade konnte nicht auf den Raum angewendet werden.");

        addMessage(Messages.UPGRADE_REMOVED, "Upgrade aus dem Raum entfernt.");
        addMessage(Messages.UPGRADE_REM_FAILED, "Die Upgradeentfernung konnte nicht auf den Raum angewendet werden.");

        addTooltip(Tooltips.ROOM_UPGRADE_TYPE, "Typ: %s");
        addTooltip(Tooltips.TUTORIAL_APPLY_ROOM_UPGRADE, "Auf einem gebundenen Maschinenblock verwenden, um ein Upgrade anzuwenden.");
        //endregion

        addCommand(CMCommands.CANNOT_GIVE_MACHINE, "Dem Spieler konnte keine neue Maschine gegeben werden.");
        addCommand(CMCommands.MACHINE_GIVEN, "Ein neues Maschinenelement erstellt und an %s übergeben.");

        addMessage(Messages.UNKNOWN_ROOM_CHUNK, "Unbekannter Raum bei %s; Bitte überprüfen Sie, ob es existiert.");

        add("itemGroup." + Constants.MOD_ID, "Kompakte Maschine");

        add("biome." + Constants.MOD_ID + ".machine", "Compact Machine");

        add("compactmachines.psd.pages.machines.title", "Kompakte Maschinen");
        add("compactmachines.psd.pages.machines", "Kompakte Maschinen sind die Kernmechanik dieses Mods." +
                "Sie ermöglichen den Bau großer Räume in einem einzigen Block, der mit der Außenwelt verbunden ist. Es gibt sie in verschiedenen Größen von 3x3x3 bis 13x13x13.\n\n" +
                "Sie können Tunnel verwenden, um die äußeren Blockflächen mit den Innenwänden zu verbinden und so Gegenstände, Flüssigkeiten usw. zu transportieren.\n\n" +
                "Sie können eine Kompaktmaschine betreten, indem Sie mit einem persönlichen Schrumpfgerät mit der rechten Maustaste darauf klicken. Bitte verwenden Sie JEI, um nach den Craftingrezepten zu suchen.");

        add("jei.compactmachines.machines", "Die Maschinen erschaffen die Taschen-Dimensionen. Crafte eine Maschine und platziere sie in der Welt. Benutze dann ein persönliches Schrumpfgerät, um hineinzugehen.");
        add("jei.compactmachines.shrinking_device", "Verwenden Sie das Persönliche Schrumpfgerät (PSD) an einer Maschine, um einen kompakten Raum zu betreten. " +
                "Sie können auch mit der rechten Maustaste in der Oberwelt darauf klicken, um weitere Informationen zu erhalten.");
        add("death.attack." + VoidAirBlock.DAMAGE_SOURCE.msgId, "%1$s konnte das Nichts nicht betreten");

        add("curios.identifier.psd", "Persönliches Schrumpfgerät");

        add(MachineRoomUpgrades.WORKBENCH_BLOCK.get(), "Werkbank");
        add("entity.minecraft.villager.compactmachines.tinkerer", "Bastler");

        add(RoomExitKeyMapping.CATEGORY, "Kompakte Maschine");
        add(RoomExitKeyMapping.NAME, " Kompakte Maschine");
    }

    @Override
    protected String getSizeTranslation(RoomSize size) {
        return capitalize(size.getSerializedName());
    }
}
