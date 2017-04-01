package org.dave.cm2.integration;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.dave.cm2.utility.AnnotatedInstanceUtil;
import org.dave.cm2.utility.Logz;

import java.util.HashMap;

public class CapabilityNullHandlerRegistry {
    private static HashMap<Capability, AbstractNullHandler> nullHandlers = new HashMap<>();

    public static void registerNullHandlers(ASMDataTable asmData) {
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
}
