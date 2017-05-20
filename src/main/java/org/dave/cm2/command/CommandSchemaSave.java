package org.dave.cm2.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.schema.BlockInformation;
import org.dave.cm2.schema.Schema;
import org.dave.cm2.utility.SerializationHelper;
import org.dave.cm2.world.tools.StructureTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CommandSchemaSave extends CommandBaseExt {

    @Override
    public String getCommandName() {
        return "save";
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
        String sane = args[0].replaceAll("[^a-zA-Z0-9\\._]+", "_") + ".json";

        if(sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityLivingBase) {
            if(sender.getEntityWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
                throw this.getException(sender, "not_in_machine_dimension");
            }

            int coords = StructureTools.getCoordsForPos(sender.getCommandSenderEntity().getPosition());


            List<BlockInformation> blockList = StructureTools.getSchema(coords);

            if(blockList != null) {
                Schema schema = new Schema(args[0], blockList);

                try {
                    File schemaFile = new File(ConfigurationHandler.schemaDirectory, sane);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(schemaFile));
                    String json = SerializationHelper.GSON.toJson(schema);
                    writer.write(json);
                    writer.close();

                    // TODO: Localization
                    sender.addChatMessage(new TextComponentString("Wrote schema to file: " + sane));
                } catch (IOException e) {
                    throw this.getException(sender, "invalid_file");
                }
            } else {
                throw this.getException(sender, "not_serializable");
            }
        }
    }
}
