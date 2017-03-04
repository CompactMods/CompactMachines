package org.dave.cm2.world;

import com.google.common.collect.ImmutableSetMultimap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.world.tools.DimensionTools;
import org.dave.cm2.utility.Logz;

import java.util.ArrayList;
import java.util.List;

public class ChunkLoadingMachines implements LoadingCallback {

    public static boolean isMachineChunkLoaded(int coords) {
        if(coords == -1) {
            return false;
        }

        // Find the ticket that is being used for this machines chunk
        ImmutableSetMultimap<ChunkPos, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(DimensionTools.getServerMachineWorld());

        ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
        for(Ticket ticket : existingTickets.values()) {
            if(visitedTickets.contains(ticket.hashCode())) {
                continue;
            }
            visitedTickets.add(ticket.hashCode());

            NBTTagCompound data = ticket.getModData();
            if(data.hasKey("coords")) {
                int[] nbtCoords = data.getIntArray("coords");

                for (int i = 0; i < nbtCoords.length; i++) {
                    if(nbtCoords[i] == coords) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static void unforceChunk(int coord) {
        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        ImmutableSetMultimap<ChunkPos, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(machineWorld);

        ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
        for(Ticket ticket : existingTickets.values()) {
            if(visitedTickets.contains(ticket.hashCode())) {
                continue;
            }
            visitedTickets.add(ticket.hashCode());

            NBTTagCompound data = ticket.getModData();
            if(data.hasKey("coords")) {
                int[] nbtCoords = data.getIntArray("coords");

                for (int i = 0; i < nbtCoords.length; i++) {
                    if(nbtCoords[i] != coord) {
                        continue;
                    }

                    Logz.debug("ChunkLoading: Runtime: Unforcing chunk for machine: %d", coord);
                    ForgeChunkManager.unforceChunk(ticket, new ChunkPos(coord << 10-4, 0));

                    int usedChunks = 0;
                    if(data.hasKey("usedChunks")) {
                        usedChunks = data.getInteger("usedChunks");
                    }

                    if(usedChunks < 2) {
                        ForgeChunkManager.releaseTicket(ticket);
                    } else {
                        nbtCoords[i] = -1;
                        data.setInteger("usedChunks", usedChunks - 1);
                        data.setIntArray("coords", nbtCoords);
                    }

                    return;
                }
            }
        }
    }

    public static void forceChunk(int coord) {
        Ticket chunkTicket = null;
        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        ImmutableSetMultimap<ChunkPos, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(machineWorld);

        ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
        for(Ticket ticket : existingTickets.values()) {
            if(visitedTickets.contains(ticket.hashCode())) {
                continue;
            }
            visitedTickets.add(ticket.hashCode());

            NBTTagCompound data = ticket.getModData();
            if (data.hasKey("coords")) {
                // Found a ticket that belongs to our mod, this should be true for all cases

                int usedChunks = 0;
                if (data.hasKey("usedChunks")) {
                    usedChunks = data.getInteger("usedChunks");
                }

                if (usedChunks < ticket.getMaxChunkListDepth()) {
                    chunkTicket = ticket;
                    break;
                }
            }
        }

        if (chunkTicket == null) {
            // No existing/free ticket found. Requesting a new one.
            chunkTicket = ForgeChunkManager.requestTicket(CompactMachines2.instance, machineWorld, ForgeChunkManager.Type.NORMAL);
        }

        if (chunkTicket == null) {
            return;
        }

        NBTTagCompound data = chunkTicket.getModData();
        int usedChunks = data.hasKey("usedChunks") ? data.getInteger("usedChunks") : 0;

        int[] nbtCoords = new int[chunkTicket.getMaxChunkListDepth()];
        if (data.hasKey("coords")) {
            nbtCoords = data.getIntArray("coords");
        } else {
            // initialize with -1
            for (int i = 0; i < nbtCoords.length; i++) {
                nbtCoords[i] = -1;
            }
        }

        // Find "slot" in ticket:
        for (int i = 0; i < nbtCoords.length; i++) {
            if (nbtCoords[i] == -1) {
                nbtCoords[i] = coord;
                break;
            }
        }

        // Each ticket needs to remember for which areas it is responsible
        data.setIntArray("coords", nbtCoords);
        data.setInteger("usedChunks", usedChunks + 1);

        Logz.debug("ChunkLoading: Runtime: Forcing chunk for machine: %d", coord);
        ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(coord << 10-4, 0));
    }


    /**
     * Called back when tickets are loaded from the world to allow the
     * mod to re-register the chunks associated with those tickets. The list supplied
     * here is truncated to length prior to use. Tickets unwanted by the
     * mod must be disposed of manually unless the mod is an OrderedLoadingCallback instance
     * in which case, they will have been disposed of by the earlier callback.
     *
     * @param tickets The tickets to re-register. The list is immutable and cannot be manipulated directly. Copy it first.
     * @param world   the world
     */
    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        if(ConfigurationHandler.Settings.forceLoadChunks) {
            Logz.info("Chunkloading is in always mode. Loading all previously loaded chunks.");
            for (Ticket ticket : tickets) {
                NBTTagCompound data = ticket.getModData();
                if(data.hasKey("coords")) {
                    int[] nbtCoords = data.getIntArray("coords");

                    boolean foundMatch = false;
                    for (int i = 0; i < nbtCoords.length; i++) {
                        if(nbtCoords[i] == -1) {
                            continue;
                        }

                        //LogHelper.info("Forcing chunk for room: " + nbtCoords[i]);
                        Logz.debug("ChunkLoading, Tickets Loaded: Forcing chunk for machine: %d", nbtCoords[i]);
                        ForgeChunkManager.forceChunk(ticket, new ChunkPos(nbtCoords[i] << 10-4, 0));
                        foundMatch = true;
                    }

                    // Ticket has no valid coords stored, releasing it.
                    if(!foundMatch) {
                        Logz.debug("ChunkLoading, Tickets Loaded: Ticket %s has no coords stored. Releasing it.", ticket);
                        ForgeChunkManager.releaseTicket(ticket);
                    }
                }
            }
        } else {
            Logz.info("Chunkloading is in smart mode. Releasing previously requested tickets...");
            for (Ticket ticket : tickets) {
                ForgeChunkManager.releaseTicket(ticket);
            }
            return;
        }
    }
}
