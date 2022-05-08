package dev.compactmods.machines.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;

import static net.minecraft.commands.arguments.coordinates.WorldCoordinate.ERROR_EXPECTED_INT;

public class RoomPositionArgument implements ArgumentType<RoomCoordinates> {
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos2d.incomplete"));

    public static RoomPositionArgument room() {
        return new RoomPositionArgument();
    }

    public static ChunkPos get(CommandContext<CommandSourceStack> ctx, String room) {
        var arg = ctx.getArgument(room, RoomCoordinates.class);
        return arg.get(ctx.getSource());
    }

    @Override
    public RoomCoordinates parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        if (!reader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(reader);
        } else {
            WorldCoordinate chunkX = WorldCoordinate.parseInt(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                WorldCoordinate chunkZ = WorldCoordinate.parseInt(reader);
                return new RoomCoordinates(chunkX, chunkZ);
            } else {
                reader.setCursor(i);
                throw ERROR_NOT_COMPLETE.createWithContext(reader);
            }
        }
    }
}
