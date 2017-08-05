package org.dave.compactmachines3.command;

import com.google.gson.stream.JsonReader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.schema.BlockInformation;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.utility.SerializationHelper;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

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
        String sane = args[0].replaceAll("[^a-zA-Z0-9\\._]+", "_") + ".json";

        File schemaFile = new File(ConfigurationHandler.schemaDirectory, sane);
        Schema schema = null;
        try {
            schema = SerializationHelper.GSON.fromJson(new JsonReader(new FileReader(schemaFile)), Schema.class);
        } catch (FileNotFoundException e) {
            throw this.getException(sender, "invalid_file");
        }

        if(schema == null) {
            throw this.getException(sender, "not_deserializable");
        }

        List<BlockInformation> blockList = schema.blocks;

        if(sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityLivingBase) {
            if (sender.getEntityWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
                throw this.getException(sender, "not_in_machine_dimension");
            }

            int coords = StructureTools.getCoordsForPos(sender.getCommandSenderEntity().getPosition());

            TileEntity te = WorldSavedDataMachines.INSTANCE.getMachinePosition(coords).getTileEntity();
            if(te != null && te instanceof TileEntityMachine) {
                WorldServer machineWorld = DimensionTools.getServerMachineWorld();

                TileEntityMachine machine = (TileEntityMachine) te;
                int size = machine.getSize().getDimension();
                int startX = machine.coords * 1024 + size - 1;
                int startY = 40 + size - 1;
                int startZ = 0 + size - 1;

                for(BlockInformation bi : blockList) {
                    BlockPos absolutePos = new BlockPos(
                            startX - bi.position.getX(),
                            startY - bi.position.getY(),
                            startZ - bi.position.getZ()
                    );

                    IBlockState state = bi.block.getStateFromMeta(bi.meta);
                    machineWorld.setBlockState(absolutePos, state);

                    if(bi.nbt != null) {
                        TileEntity restoredTe = machineWorld.getTileEntity(absolutePos);
                        if (restoredTe == null) {
                            restoredTe = bi.block.createTileEntity(machineWorld, state);
                        }

                        if(bi.writePositionData) {
                            bi.nbt.setInteger("x", absolutePos.getX());
                            bi.nbt.setInteger("y", absolutePos.getY());
                            bi.nbt.setInteger("z", absolutePos.getZ());
                        }

                        restoredTe.readFromNBT(bi.nbt);
                        restoredTe.markDirty();
                    }
                }
            }
        }
    }
}
