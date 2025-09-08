package dev.compactmods.machines.api.room.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.location.GlobalPosWithRotation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RoomEntryPoint(GlobalPosWithRotation entryLocation, EntryMethod method) {

    public static final RoomEntryPoint INVALID = new RoomEntryPoint(GlobalPosWithRotation.INVALID, EntryMethod.OTHER);


    public static final MapCodec<RoomEntryPoint> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            GlobalPosWithRotation.CODEC.fieldOf("entry_location").forGetter(RoomEntryPoint::entryLocation),
            EntryMethod.CODEC.fieldOf("method").forGetter(RoomEntryPoint::method)
    ).apply(i, RoomEntryPoint::new));

    public static RoomEntryPoint playerEnteringMachine(Player player) {
        return new RoomEntryPoint(GlobalPosWithRotation.fromPlayer(player), EntryMethod.SHRINKING_DEVICE);
    }

    public static RoomEntryPoint playerUsingCommand(Player player) {
        return new RoomEntryPoint(GlobalPosWithRotation.fromPlayer(player), EntryMethod.COMMAND);
    }

    /**
     * Represents an invalid position, mostly used for "blank" data and testing.
     * @return a new entry point.
     */
    public static RoomEntryPoint nonexistent() {
        return new RoomEntryPoint(GlobalPosWithRotation.INVALID, EntryMethod.OTHER);
    }

    public enum EntryMethod implements StringRepresentable {
        SHRINKING_DEVICE("psd", true),
        ADVANCED_SHRINKING_DEVICE("advanced_psd", true),
        TELEPORTED_VIA_MOD("teleport_mod", false),
        VANILLA_PORTAL("portal", false),
        COMMAND("command", true),
        OTHER("other", false);

        public static final Codec<EntryMethod> CODEC = StringRepresentable.fromEnum(EntryMethod::values);

        private final String name;

        /**
         * Whether the entry point should be treated as a search area or a precise location to respawn
         * a player.
         */
        private final boolean preciseExit;

        EntryMethod(String name, boolean preciseExit) {
            this.name = name;
            this.preciseExit = preciseExit;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        public boolean isPreciseExit() {
            return preciseExit;
        }
    }
}