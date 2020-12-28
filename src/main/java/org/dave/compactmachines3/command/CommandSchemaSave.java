package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.schema.BlockInformation;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;
import org.dave.compactmachines3.utility.SerializationHelper;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CommandSchemaSave extends CommandBaseExt {

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return creative && isOp;
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

            int id = StructureTools.getIdForPos(sender.getCommandSenderEntity().getPosition());

            List<BlockInformation> blockList = StructureTools.createNewSchema(id);

            if(blockList != null) {
                Schema schema = new Schema(args[0]);
                schema.setBlocks(blockList);
                schema.setSize(WorldSavedDataMachines.getInstance().machineSizes.get(id));

                BlockPos roomPos = WorldSavedDataMachines.getInstance().getMachineRoomPosition(id);
                Vec3d pos = sender.getPositionVector();
                schema.setSpawnPosition(new Vec3d(pos.x - roomPos.getX(), pos.y - roomPos.getY(), pos.z - roomPos.getZ()));

                try {
                    File schemaFile = new File(ConfigurationHandler.schemaDirectory, sane);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(schemaFile));
                    String json = SerializationHelper.GSON.toJson(schema);
                    writer.write(json);
                    writer.close();

                    sender.sendMessage(new TextComponentTranslation("commands.compactmachines3.schema.save.success", sane));
                } catch (IOException e) {
                    throw this.getException(sender, "invalid_file");
                }

                SchemaRegistry.instance.addSchema(schema);
            } else {
                throw this.getException(sender, "not_serializable");
            }
        }
    }
}
