package org.dave.compactmachines3.world;

import com.google.common.collect.ImmutableSetMultimap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.world.tools.DimensionTools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkLoadingMachines implements LoadingCallback {
    public static boolean isMachineChunkLoaded(int id, BlockPos roomPos) {
        if (id == -1)
            return false;

        // The set of tickets that are being used to chunkload the room chunk, or empty if none
        Set<Ticket> chunkTickets = getPersistentChunksForMachineWorld().get(new ChunkPos(roomPos));

        for (Ticket ticket : chunkTickets) {
            NBTTagCompound data = ticket.getModData();
            if (isInvalid(ticket))
                continue;

            int[] nbtIds = data.hasKey("id") ? data.getIntArray("id") : data.getIntArray("coords");

            for (int nbtId : nbtIds) {
                if (nbtId == id) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void unforceChunk(int id, BlockPos roomPos) {
        if (id == -1 || roomPos == null)
            return;

        // The set of tickets that are being used to chunkload the room chunk, or empty if none
        ChunkPos chunkPos = new ChunkPos(roomPos);
        Set<Ticket> chunkTickets = getPersistentChunksForMachineWorld().get(chunkPos);

        for (Ticket ticket : chunkTickets) {
            NBTTagCompound data = ticket.getModData();
            if (isInvalid(ticket))
                continue;

            int[] nbtIds = data.hasKey("id") ? data.getIntArray("id") : data.getIntArray("coords");
            int usedChunks = data.getInteger("usedChunks"); // Defaults to 0 if doesn't exist

            for (int i = 0; i < nbtIds.length; i++) {
                if (nbtIds[i] != id) {
                    continue;
                }

                CompactMachines3.logger.debug("ChunkLoading: Runtime: Unforcing chunk for machine: {}", id);
                ForgeChunkManager.unforceChunk(ticket, chunkPos);

                if (usedChunks <= 1) { // If there is only this chunk left, we should release this ticket
                    ForgeChunkManager.releaseTicket(ticket);
                } else {
                    nbtIds[i] = -1;
                    data.setInteger("usedChunks", usedChunks - 1);
                    data.setIntArray("id", nbtIds);
                }

                return;
            }
        }
    }

    public static void forceChunk(int id, BlockPos roomPos) {
        if (id == -1 || roomPos == null)
            return;

        Ticket chunkTicket = null;
        for (Ticket ticket : getModTicketsForMachineWorld()) {
            NBTTagCompound data = ticket.getModData();
            int usedChunks = data.getInteger("usedChunks"); // Defaults to 0 if doesn't exist

            if (usedChunks < ticket.getMaxChunkListDepth()) {
                chunkTicket = ticket;
                break;
            }
        }

        if (chunkTicket == null) {
            // No existing/free ticket found. Requesting a new one.
            WorldServer machineWorld = DimensionTools.getServerMachineWorld();
            chunkTicket = ForgeChunkManager.requestTicket(CompactMachines3.instance, machineWorld, ForgeChunkManager.Type.NORMAL);
        }

        if (chunkTicket == null) { // Still null, we can't force this chunk
            return;
        }

        NBTTagCompound data = chunkTicket.getModData();
        int usedChunks = data.getInteger("usedChunks"); // Defaults to 0 if doesn't exist

        int[] nbtIds = new int[chunkTicket.getMaxChunkListDepth()];
        Arrays.fill(nbtIds, -1); // Initialize with -1
        if (data.hasKey("id") || data.hasKey("coords")) {
            // The NBT array length might be smaller than the max chunk list depth;
            // This ensures the same size is kept but the values are still put inside the array
            int[] tempIds = data.hasKey("id") ? data.getIntArray("id") : data.getIntArray("coords");
            System.arraycopy(tempIds, 0, nbtIds, 0, tempIds.length);
        }

        // Find "slot" in ticket:
        for (int i = 0; i < nbtIds.length; i++) {
            if (nbtIds[i] == -1) {
                nbtIds[i] = id;
                break;
            }
        }

        // Each ticket needs to remember for which areas it is responsible
        data.setIntArray("id", nbtIds);
        data.setInteger("usedChunks", usedChunks + 1);

        CompactMachines3.logger.debug("ChunkLoading: Runtime: Forcing chunk for machine: {}", id);
        ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(roomPos));
    }

    private static ImmutableSetMultimap<ChunkPos, Ticket> getPersistentChunksForMachineWorld() {
        return ForgeChunkManager.getPersistentChunksFor(DimensionTools.getServerMachineWorld());
    }

    private static Set<Ticket> getModTicketsForMachineWorld() {
        Set<Ticket> tickets = new HashSet<>();
        Set<Ticket> seen = new HashSet<>();
        for (Ticket ticket : getPersistentChunksForMachineWorld().values()) {
            if (!seen.add(ticket))
                continue; // We've already seen it if Set#add is false
            if (CompactMachines3.MODID.equals(ticket.getModId())) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    /**
     * Called back when tickets are loaded from the world to allow the
     * mod to re-register the chunks associated with those tickets. The list supplied
     * here is truncated to length prior to use. Tickets unwanted by the
     * mod must be disposed of manually unless the mod is an OrderedLoadingCallback instance
     * in which case, they will have been disposed of by the earlier callback.
     *
     * @param tickets The tickets to re-register. The list is immutable and cannot be manipulated directly. Copy it first.
     * @param world the world
     */
    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        if (ConfigurationHandler.Settings.forceLoadChunks) {
            CompactMachines3.logger.info("Chunkloading is in always mode. Loading all previously loaded chunks.");
            for (Ticket ticket : tickets) {
                NBTTagCompound data = ticket.getModData();
                if (isInvalid(ticket))
                    continue;
                int[] nbtIds = data.hasKey("id") ? data.getIntArray("id") : data.getIntArray("coords");

                boolean foundMatch = false;
                for (int nbtId : nbtIds) {
                    if (nbtId == -1) {
                        continue;
                    }

                    //LogHelper.info("Forcing chunk for room: " + nbtIds[i]);
                    CompactMachines3.logger.debug("ChunkLoading, Tickets Loaded: Forcing chunk for machine: {}", nbtId);
                    BlockPos roomPos = WorldSavedDataMachines.getInstance().getMachineRoomPosition(nbtId);
                    if (roomPos == null)
                        continue;
                    ForgeChunkManager.forceChunk(ticket, new ChunkPos(roomPos));
                    foundMatch = true;
                }

                // Ticket has no valid ids stored, releasing it.
                if (!foundMatch) {
                    CompactMachines3.logger.debug("ChunkLoading, Tickets Loaded: Ticket {} has no ids stored. Releasing it.", ticket);
                    ForgeChunkManager.releaseTicket(ticket);
                }
            }
        } else {
            CompactMachines3.logger.info("Chunkloading is in smart mode. Releasing previously requested tickets...");
            for (Ticket ticket : tickets) {
                if (!CompactMachines3.MODID.equals(ticket.getModId())) // Not our mod
                    continue;
                ForgeChunkManager.releaseTicket(ticket);
            }
        }
    }

    private static boolean isInvalid(Ticket ticket) {
        if (!CompactMachines3.MODID.equals(ticket.getModId())) // Not our mod so it's invalid
            return true;

        NBTTagCompound data = ticket.getModData();
        return !data.hasKey("coords") && !data.hasKey("id"); // Returns true if it doesn't have one of these keys (aka invalid)
    }
}
