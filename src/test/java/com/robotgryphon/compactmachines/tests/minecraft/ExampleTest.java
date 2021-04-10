package com.robotgryphon.compactmachines.tests.minecraft;

import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.SavedMachineData;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExampleTest {

    @Test
    void CanDoBasicTest() {
        MinecraftServer serv = ServerLifecycleHooks.getCurrentServer();
        // SavedMachineData sd = SavedMachineData.getInstance(serv);

        DimensionSavedDataManager ds = serv
                .getLevel(Registration.COMPACT_DIMENSION)
                .getDataStorage();

        SavedMachineData found = ds.get(SavedMachineData::new, SavedMachineData.DATA_NAME);
        if(found != null) {
            // Assertions.assertNotNull(sd);
            Method getDataFile = ObfuscationReflectionHelper.findMethod(
                    DimensionSavedDataManager.class,
                    "func_215754_a",
                    String.class);

            try {
                File i = (File) getDataFile.invoke(ds, found.getId());
                System.out.println(i.getAbsolutePath());

                i.delete();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            // File not found
            Assertions.assertTrue(true);
        }
    }
}
