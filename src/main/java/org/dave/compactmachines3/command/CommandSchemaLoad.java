package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.StructureTools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSchemaLoad extends CommandBaseExt {
    @Override
    public String getName() {
        return "load";
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

        if(!SchemaRegistry.instance.hasSchema(args[0])) {
            throw this.getException(sender, "unknown_schema");
        }

        if (sender.getEntityWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            throw this.getException(sender, "not_in_machine_dimension");
        }

        Schema schema = SchemaRegistry.instance.getSchema(args[0]);
        int coords = StructureTools.getCoordsForPos(sender.getCommandSenderEntity().getPosition());
        EnumMachineSize machineSize = WorldSavedDataMachines.INSTANCE.machineSizes.get(coords);
        if(machineSize != schema.getSize()) {
            throw this.getException(sender, "machine_size_does_not_match");
        }

        sender.sendMessage(new TextComponentTranslation("commands.compactmachines3.schema.load.machine_schema_set_to", args[0]));
        StructureTools.restoreSchema(schema, coords);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return SchemaRegistry.instance.getSchemaNames().stream().filter(s -> args.length == 0 || s.startsWith(args[0])).collect(Collectors.toList());
    }
}
