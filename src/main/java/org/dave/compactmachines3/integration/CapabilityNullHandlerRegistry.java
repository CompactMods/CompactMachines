package org.dave.compactmachines3.integration;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.dave.compactmachines3.utility.AnnotatedInstanceUtil;
import org.dave.compactmachines3.utility.Logz;

import java.util.HashMap;

public class CapabilityNullHandlerRegistry {
    private static HashMap<Capability, AbstractNullHandler> nullHandlers = new HashMap<>();
    private static ASMDataTable asmData;

    public static void registerNullHandlers() {
        for(AbstractNullHandler nh : AnnotatedInstanceUtil.getNullHandlers(asmData)) {
            if(nullHandlers.containsKey(nh.getCapability())) {
                continue;
            }

            Logz.info("Registered null handler for capability: %s", nh.getCapability().getName());
            nullHandlers.put(nh.getCapability(), nh);
        }
    }

    public static boolean hasNullHandler(Capability capability) {
        return nullHandlers.containsKey(capability);
    }

    public static <T> T getNullHandler(Capability<T> capability) {
        return (T) nullHandlers.get(capability);
    }

    public static void setAsmData(ASMDataTable asmData) {
        CapabilityNullHandlerRegistry.asmData = asmData;
    }
}
