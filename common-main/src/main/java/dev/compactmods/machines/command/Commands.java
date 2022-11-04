package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.api.core.Constants;
import net.minecraft.commands.CommandSourceStack;


public class Commands {

    // TODO: /cm create <size:RoomSize> <owner:Player> <giveMachine:true|false>
    // TODO: /cm spawn set <room> <pos>

    static final LiteralArgumentBuilder<CommandSourceStack> CM_COMMAND_ROOT
            = LiteralArgumentBuilder.literal(Constants.MOD_ID);

    public static void prepare() {

    }

    public static LiteralArgumentBuilder<CommandSourceStack> getRoot() {
        return CM_COMMAND_ROOT;
    }
}
