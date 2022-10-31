package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.room.RoomTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * @deprecated These templates will be removed in 1.20, and the system will become fully data controlled.
 * Do not assume these are valid at any point!
 */
@Deprecated(forRemoval = true, since = "5.2.0")
public enum LegacySizedTemplates {
    EMPTY_TINY("tiny", 3, FastColor.ARGB32.color(255, 201, 91, 19)),
    EMPTY_SMALL("small", 5, FastColor.ARGB32.color(255, 212, 210, 210)),
    EMPTY_NORMAL("normal", 7, FastColor.ARGB32.color(255, 251, 242, 54)),
    EMPTY_LARGE("large", 9, FastColor.ARGB32.color(255, 33, 27, 46)),
    EMPTY_GIANT("giant", 11, FastColor.ARGB32.color(255, 67, 214, 205)),
    EMPTY_COLOSSAL("colossal", 13, FastColor.ARGB32.color(255, 66, 63, 66));

    private final ResourceLocation id;
    private final RoomTemplate template;

    LegacySizedTemplates(String id, int size, int color) {
        this.id = new ResourceLocation(Constants.MOD_ID, id);
        this.template = new RoomTemplate(size, color);
    }

    public RoomTemplate template() {
        return template;
    }

    public ResourceLocation id() {
        return id;
    }
}
