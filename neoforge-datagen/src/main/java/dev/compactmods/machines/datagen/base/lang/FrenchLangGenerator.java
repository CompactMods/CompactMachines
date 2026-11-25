package dev.compactmods.machines.datagen.base.lang;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.i18n.Translations;
import dev.compactmods.machines.api.advancement.Advancements;
import dev.compactmods.machines.i18n.CommandTranslations;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.i18n.RoomTranslations;
import dev.compactmods.machines.client.keybinds.room.RoomExitKeyMapping;
import dev.compactmods.machines.client.creative.CreativeTabs;
import dev.compactmods.machines.client.keybinds.room.RoomUpgradeUIMapping;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;

public class FrenchLangGenerator extends dev.compactmods.machines.datagen.base.lang.BaseLangGenerator {
    public FrenchLangGenerator(PackOutput packOutput) {
        super(packOutput, "fr_fr");
    }

    @Override
    protected String getMachineTranslation() {
        return "Compact Machine";
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();

        blocksAndItems();

        add(Translations.IDs.HOW_DID_YOU_GET_HERE, "Comment es-tu arrivé ici ?!");
        add(Translations.IDs.TELEPORT_OUT_OF_BOUNDS, "Une force d'un autre monde empêche votre téléportation.");

        // Machine Translations
        add(MachineTranslations.IDs.OWNER, "Propriétaire : %s");
        add(MachineTranslations.IDs.SIZE, "Taille interne : %1$s");
        add(MachineTranslations.IDs.BOUND_TO, "Liée à : %1$s");
        add(MachineTranslations.IDs.NEW_MACHINE, "Nouvelle Machine");

        // Room Translations
        add(RoomTranslations.IDs.ROOM_SPAWNPOINT_SET, "Nouveau point d'apparition défini.");
        add(RoomTranslations.IDs.MACHINE_ROOM_INFO, "La machine en %1$s est liée à une salle de taille %2$s à %3$s");
        add(RoomTranslations.IDs.PLAYER_ROOM_INFO, "Le joueur '%1$s' se trouve dans la salle %2$s.");

        // Room Errors
        add(RoomTranslations.IDs.Errors.CANNOT_ENTER_ROOM, "Vous tripotez le dispositif de rétrécissement, en vain. Il refuse de fonctionner.");
        add(RoomTranslations.IDs.Errors.UNKNOWN_ROOM_BY_CODE, "La salle [%s] est introuvable.");

        // Room Templates
        add(RoomTemplate.I18N_STRUCTURE_GEN_TOOLTIP, "Génère des structures %s lors de la création de la salle.");
        add(RoomTemplate.I18N_INTERNAL_ROOM_DIMS, "Taille interne : %s");


        commands();
        advancements();

        add(CompactMachines.MOD_ID + ".direction.side", "Côté : %s");
        add(CompactMachines.MOD_ID + ".connected_block", "Connecté : %s");

        add(Translations.IDs.UNBREAKABLE_BLOCK, "Attention ! Incassable pour les joueurs non créatifs !");
        add(Translations.IDs.HINT_HOLD_SHIFT, "Maintenez Maj pour plus de détails.");

        addCreativeTab(CreativeTabs.MAIN_RL, "Compact Machines");

        add("biome." + CompactMachines.MOD_ID + ".machine", "Compact Machine");

        add("jei.compactmachines.machines", "Les machines permettent de créer des dimensions de poche. Fabriquez une machine, placez-la dans le monde, puis utilisez un dispositif de rétrécissement rersonnel pour y entrer.");
        add("jei.compactmachines.shrinking_device", "Utilisez le dispositif de rétrécissement personnel (DRP) sur une machine pour entrer dans un espace compact.");

        add("curios.identifier.psd", "Dispositif de rétrécissement personnel");

        add("entity.minecraft.villager.compactmachines.tinkerer", "Bricoleur Spatial");

        add(RoomExitKeyMapping.I18n.CATEGORY, "Compact Machines");
        add(RoomExitKeyMapping.I18n.NAME, "Sortie Rapide de Compact Machine");
        add(RoomUpgradeUIMapping.NAME, "Ouvrir l'écran d'amélioration de salle");

        addJade();

        addGamerule(CMGameRules.ALLOW_SURVIVAL_OUT_OF_BOUNDS_KEY, "Autoriser survie hors limites", "Autorise les joueurs en survie en dehors des limites");
        addGamerule(CMGameRules.ALLOW_CREATIVE_OUT_OF_BOUNDS_KEY, "Autoriser créatif hors limites", "Autorise les joueurs en créatif en dehors des limites");
        addGamerule(CMGameRules.ALLOW_SPECTATORS_OUT_OF_BOUNDS_KEY, "Autoriser spectateurs hors limites", "Autorise les spectateurs en dehors des limites");
        addGamerule(CMGameRules.DAMAGE_OOB_PLAYERS_KEY, "Endommager joueurs hors limites", "Inflige des dégâts aux joueurs en dehors des limites");
        addGamerule(CMGameRules.DAMAGE_PSD_ITEMS_ON_ROOM_EXIT_KEY, "Endommager les DRP à la sortie", "Endommage les dispositifs de rétrécissement en quittant une salle");
    }

    private void blocksAndItems() {
        final var machineTranslation = getMachineTranslation();

        addBlock(Rooms.Blocks.BREAKABLE_WALL, "Mur de Compact Machine");
        addBlock(Rooms.Blocks.SOLID_WALL, "Mur Solide de Compact Machine");

        addItem(Shrinking.PERSONAL_SHRINKING_DEVICE, "Dispositif de rétrécissement personnel");
        addItem(Shrinking.SHRINKING_MODULE, "Module de rétrécissement atomique");
        addItem(Shrinking.ENLARGING_MODULE, "Module d'agrandissement atomique");
        add(Util.makeDescriptionId("block", CompactMachines.modRL("bound_machine_fallback")), machineTranslation);
    }

    protected void advancements() {
        advancement(Advancements.ROOT, "Machines Compactes", "");
        advancement(Advancements.FOUNDATIONS, "Fondations", "Obtenir un bloc de mur destructible.");
        advancement(Advancements.GOT_SHRINKING_DEVICE, "Dispositif de rétrécissement personnel", "Obtenir un dispositif de rétrécissement Personnel");
        advancement(Advancements.HOW_DID_YOU_GET_HERE, "Comment es-tu Arrivé Ici ?!", "Dans quelle machine se trouve le joueur ?!");
        advancement(Advancements.RECURSIVE_ROOMS, "Salles Récursives", "Pour comprendre la récursivité, il faut d'abord comprendre la récursivité.");
    }

    private void commands() {
        add(CommandTranslations.IDs.CANNOT_GIVE_MACHINE, "Échec de l'attribution d'une nouvelle machine au joueur.");
        add(CommandTranslations.IDs.MACHINE_GIVEN, "Un nouvel objet machine a été créé et donné à %s.");
        add(CommandTranslations.IDs.ROOM_COUNT, "Nombre de salles enregistrées : %s");
        add(CommandTranslations.IDs.SPAWN_CHANGED_SUCCESSFULLY, "Le point d'apparition de la salle [%s] a été modifié avec succès.");
    }

    private void addJade() {
        add("config.jade.plugin_compactmachines.bound_machine", "Machines compactes liées");
        add("config.jade.plugin_compactmachines.show_owner", "Afficher les propriétaires de machines");
    }
}
