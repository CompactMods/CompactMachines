package org.dave.compactmachines3.utility;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.dave.compactmachines3.world.data.provider.AbstractExtraTileDataProvider;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProviderRegistry;

import java.lang.reflect.Field;
import java.util.List;

public class ChunkUtils {
    private static Field updatePacketNBTField;

    public static IBlockAccess getBlockAccessFromChunk(Chunk chunk) {
        return new ChunkBlockAccess(chunk);
    }

    // This is copied straight from the AnvilChunkLoader class
    /**
     * Writes the Chunk passed as an argument to the NBTTagCompound also passed, using the World argument to retrieve
     * the Chunk's last update time.
     */
    public static NBTTagCompound writeChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound compound)
    {
        compound.setInteger("xPos", chunkIn.x);
        compound.setInteger("zPos", chunkIn.z);
        compound.setLong("LastUpdate", worldIn.getTotalWorldTime());
        compound.setIntArray("HeightMap", chunkIn.getHeightMap());
        compound.setBoolean("TerrainPopulated", chunkIn.isTerrainPopulated());
        compound.setBoolean("LightPopulated", chunkIn.isLightPopulated());
        compound.setLong("InhabitedTime", chunkIn.getInhabitedTime());
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        NBTTagList nbttaglist = new NBTTagList();
        boolean flag = worldIn.provider.hasSkyLight();


        for (ExtendedBlockStorage extendedblockstorage : aextendedblockstorage)
        {
            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Y", (byte)(extendedblockstorage.getYLocation() >> 4 & 255));
                byte[] abyte = new byte[4096];
                NibbleArray nibblearray = new NibbleArray();
                NibbleArray nibblearray1 = extendedblockstorage.getData().getDataForNBT(abyte, nibblearray);
                nbttagcompound.setByteArray("Blocks", abyte);
                nbttagcompound.setByteArray("Data", nibblearray.getData());

                if (nibblearray1 != null)
                {
                    nbttagcompound.setByteArray("Add", nibblearray1.getData());
                }

                nbttagcompound.setByteArray("BlockLight", extendedblockstorage.getBlockLight().getData());

                if (flag)
                {
                    nbttagcompound.setByteArray("SkyLight", extendedblockstorage.getSkyLight().getData());
                }
                else
                {
                    nbttagcompound.setByteArray("SkyLight", new byte[extendedblockstorage.getBlockLight().getData().length]);
                }

                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Sections", nbttaglist);
        compound.setByteArray("Biomes", chunkIn.getBiomeArray());
        chunkIn.setHasEntities(false);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int i = 0; i < chunkIn.getEntityLists().length; ++i)
        {
            for (Entity entity : chunkIn.getEntityLists()[i])
            {
                NBTTagCompound nbttagcompound2 = new NBTTagCompound();

                try
                {
                    if (entity.writeToNBTOptional(nbttagcompound2))
                    {
                        chunkIn.setHasEntities(true);
                        nbttaglist1.appendTag(nbttagcompound2);
                    }
                }
                catch (Exception e)
                {
                    net.minecraftforge.fml.common.FMLLog.log.error("An Entity type {} has thrown an exception trying to write state. It will not be visible in compact machines. Report this to the Compact Machines author.",
                            entity.getClass().getName(), e);
                }
            }
        }

        compound.setTag("Entities", nbttaglist1);

        if(updatePacketNBTField == null) {
            updatePacketNBTField = ReflectionHelper.findField(SPacketUpdateTileEntity.class, "nbt", "field_148860_e");
            updatePacketNBTField.setAccessible(true);
        }

        NBTTagList nbttaglist2 = new NBTTagList();
        for (TileEntity tileentity : chunkIn.getTileEntityMap().values())
        {
            try
            {
                NBTTagCompound nbttagcompound3 = tileentity.writeToNBT(new NBTTagCompound());
                for(AbstractExtraTileDataProvider provider : ExtraTileDataProviderRegistry.getDataProviders(tileentity)) {
                    NBTTagCompound extraData = provider.writeExtraData(tileentity);
                    String tagName = String.format("cm3_extra:%s", provider.getName());
                    nbttagcompound3.setTag(tagName, extraData);
                }

                SPacketUpdateTileEntity updatePacket = tileentity.getUpdatePacket();
                if(updatePacket != null) {
                    NBTTagCompound updateData = (NBTTagCompound) updatePacketNBTField.get(updatePacket);
                    nbttagcompound3.setTag("cm3_update", updateData);
                }

                nbttaglist2.appendTag(nbttagcompound3);
            }
            catch (Exception e)
            {
                net.minecraftforge.fml.common.FMLLog.log.error("A TileEntity type {} has throw an exception trying to write state. It will not be visible in compact machines. Report this to the Compact Machines author.",
                        tileentity.getClass().getName(), e);
            }
        }
        compound.setTag("TileEntities", nbttaglist2);


        List<NextTickListEntry> list = worldIn.getPendingBlockUpdates(chunkIn, false);
        if (list != null)
        {
            long j = worldIn.getTotalWorldTime();
            NBTTagList nbttaglist3 = new NBTTagList();

            for (NextTickListEntry nextticklistentry : list)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(nextticklistentry.getBlock());
                nbttagcompound1.setString("i", resourcelocation == null ? "" : resourcelocation.toString());
                nbttagcompound1.setInteger("x", nextticklistentry.position.getX());
                nbttagcompound1.setInteger("y", nextticklistentry.position.getY());
                nbttagcompound1.setInteger("z", nextticklistentry.position.getZ());
                nbttagcompound1.setInteger("t", (int)(nextticklistentry.scheduledTime - j));
                nbttagcompound1.setInteger("p", nextticklistentry.priority);
                nbttaglist3.appendTag(nbttagcompound1);
            }

            compound.setTag("TileTicks", nbttaglist3);
        }

        return compound;
    }

    /**
     * Reads the data stored in the passed NBTTagCompound and creates a Chunk with that data in the passed World.
     * Returns the created Chunk.
     */
    public static Chunk readChunkFromNBT(World worldIn, NBTTagCompound compound)
    {
        int i = compound.getInteger("xPos");
        int j = compound.getInteger("zPos");
        Chunk chunk = new Chunk(worldIn, i, j);
        chunk.setHeightMap(compound.getIntArray("HeightMap"));
        chunk.setTerrainPopulated(compound.getBoolean("TerrainPopulated"));
        chunk.setLightPopulated(compound.getBoolean("LightPopulated"));

        chunk.setInhabitedTime(compound.getLong("InhabitedTime"));
        NBTTagList nbttaglist = compound.getTagList("Sections", 10);
        int k = 16;
        ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[16];
        boolean flag = true;

        for (int l = 0; l < nbttaglist.tagCount(); ++l)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(l);
            int i1 = nbttagcompound.getByte("Y");
            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(i1 << 4, flag);
            byte[] abyte = nbttagcompound.getByteArray("Blocks");
            NibbleArray nibblearray = new NibbleArray(nbttagcompound.getByteArray("Data"));
            NibbleArray nibblearray1 = nbttagcompound.hasKey("Add", 7) ? new NibbleArray(nbttagcompound.getByteArray("Add")) : null;
            extendedblockstorage.getData().setDataFromNBT(abyte, nibblearray, nibblearray1);
            extendedblockstorage.setBlockLight(new NibbleArray(nbttagcompound.getByteArray("BlockLight")));

            if (flag)
            {
                extendedblockstorage.setSkyLight(new NibbleArray(nbttagcompound.getByteArray("SkyLight")));
            }

            extendedblockstorage.recalculateRefCounts();
            aextendedblockstorage[i1] = extendedblockstorage;
        }

        chunk.setStorageArrays(aextendedblockstorage);

        if (compound.hasKey("Biomes", 7))
        {
            chunk.setBiomeArray(compound.getByteArray("Biomes"));
        }

        // End this method here and split off entity loading to another method
        loadEntities(worldIn, compound, chunk);

        return chunk;
    }


    public static void loadEntities(World worldIn, NBTTagCompound compound, Chunk chunk)
    {
        NBTTagList nbttaglist1 = compound.getTagList("Entities", 10);

        for (int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist1.getCompoundTagAt(j1);
            AnvilChunkLoader.readChunkEntity(nbttagcompound1, worldIn, chunk);
            chunk.setHasEntities(true);
        }

        NBTTagList nbttaglist2 = compound.getTagList("TileEntities", 10);

        for (int k1 = 0; k1 < nbttaglist2.tagCount(); ++k1)
        {
            NBTTagCompound nbttagcompound2 = nbttaglist2.getCompoundTagAt(k1);
            TileEntity tileentity = TileEntity.create(worldIn, nbttagcompound2);
            if (tileentity != null)
            {
                tileentity.setWorld(worldIn);

                for(AbstractExtraTileDataProvider provider : ExtraTileDataProviderRegistry.getDataProviders(tileentity)) {
                    String tagName = String.format("cm3_extra:%s", provider.getName());
                    if(nbttagcompound2.hasKey(tagName)) {
                        provider.readExtraData(tileentity, (NBTTagCompound) nbttagcompound2.getTag(tagName));
                    }
                }

                chunk.addTileEntity(tileentity);

                if(nbttagcompound2.hasKey("cm3_update")) {
                    NBTTagCompound tag = nbttagcompound2.getCompoundTag("cm3_update");

                    try {
                        tileentity.onDataPacket(null, new SPacketUpdateTileEntity(tileentity.getPos(), 1, tag));
                    } catch (NullPointerException npe) {
                        Logz.debug("TileEntity '%s' is unable to read data packet without a network manager instance.", tileentity.getClass().getName());
                    } catch (Exception e) {
                        Logz.debug("TileEntity '%s' is unable to read data packet. Probably because of internally saved block positions or dimension. Report this to the Compact Machines author, not the blocks mod author!", tileentity.getClass().getName());
                    }
                }
            }
        }
    }
}
