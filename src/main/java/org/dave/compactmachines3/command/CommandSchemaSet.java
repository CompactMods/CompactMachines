package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;
import org.dave.compactmachines3.tile.TileEntityMachine;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSchemaSet extends CommandBaseExt {
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return creative || isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length != 1) {
            throw this.getUsageException(sender);
        }

        String schemaName = args[0];
        if(!SchemaRegistry.instance.hasSchema(schemaName)) {
            throw this.getException(sender, "unknown_schema");
        }

        RayTraceResult rayTraceResult = sender.getCommandSenderEntity().rayTrace(16.0f, 0.0f);
        if(rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            throw this.getException(sender, "look_at_compact_machine");
        }

        BlockPos pos = rayTraceResult.getBlockPos();
        World world = sender.getEntityWorld();

        TileEntity te = world.getTileEntity(pos);
        if(te == null || !(te instanceof TileEntityMachine)) {
            throw this.getException(sender, "look_at_compact_machine");
        }

        TileEntityMachine machine = (TileEntityMachine)te;
        if(machine.coords != -1) {
            throw this.getException(sender, "machine_is_already_in_use");
        }

        Schema schema = SchemaRegistry.instance.getSchema(schemaName);
        if(machine.getSize() != schema.getSize()) {
            throw this.getException(sender, "machine_size_does_not_match");
        }

        sender.sendMessage(new TextComponentTranslation("commands.compactmachines3.schema.set.machine_schema_set_to", schemaName));

        machine.setSchema(schemaName);
        machine.markDirty();
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return SchemaRegistry.instance.getSchemaNames().stream().filter(s -> args.length == 0 || s.startsWith(args[0])).collect(Collectors.toList());
    }
}
