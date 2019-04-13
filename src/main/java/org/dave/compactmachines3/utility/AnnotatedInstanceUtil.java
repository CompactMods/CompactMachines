package org.dave.compactmachines3.utility;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.dave.compactmachines3.integration.CapabilityNullHandler;
import org.dave.compactmachines3.integration.AbstractNullHandler;
import org.dave.compactmachines3.world.data.provider.AbstractExtraTileDataProvider;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This code is in large parts copied from the JustEnoughItems mod, which is MIT licensed.
 * You can find the original implementation here:
 * https://github.com/mezz/JustEnoughItems/blob/68242be874/src/main/java/mezz/jei/startup/AnnotatedInstanceUtil.java
 *
 * Thanks to Mezz for originally implementing this.
 *
 * When the time comes for 1.13, Mezz also already ported this. Find an updated version here:
 * https://github.com/mezz/JustEnoughItems/blob/582eb87925032083f54e0903d0afefeeae238dd5/src/main/java/mezz/jei/util/AnnotatedInstanceUtil.java
 */
public class AnnotatedInstanceUtil {
    public static ASMDataTable asmData;

    private AnnotatedInstanceUtil() {
    }

    public static List<AbstractNullHandler> getNullHandlers() {
        return getInstances(asmData, CapabilityNullHandler.class, AbstractNullHandler.class);
    }

    public static List<AbstractExtraTileDataProvider> getExtraTileDataProviders() {
        return getInstances(asmData, ExtraTileDataProvider.class, AbstractExtraTileDataProvider.class);
    }

    private static <T> List<T> getInstances(ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        List<T> instances = new ArrayList<T>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                if (annotationInfo.containsKey("mod")) {
                    String requiredMod = (String) annotationInfo.get("mod");
                    if (requiredMod.length() > 0 && !Loader.isModLoaded(requiredMod)) {
                        continue;
                    }
                }

                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                T instance = asmInstanceClass.newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException e) {
                Logz.error("Failed to load: {}", asmData.getClassName(), e);
            } catch (IllegalAccessException e) {
                Logz.error("Failed to load: {}", asmData.getClassName(), e);
            } catch (InstantiationException e) {
                Logz.error("Failed to load: {}", asmData.getClassName(), e);
            } catch (ExceptionInInitializerError e) {
                Logz.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
        return instances;
    }

    public static void setAsmData(ASMDataTable asmData) {
        AnnotatedInstanceUtil.asmData = asmData;
    }
}
