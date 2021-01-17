package org.dave.compactmachines3.integration;

import net.minecraftforge.common.capabilities.Capability;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.utility.AnnotatedInstanceUtil;

import java.util.HashMap;

public class CapabilityNullHandlerRegistry {
    private static HashMap<Capability, AbstractNullHandler> nullHandlers = new HashMap<>();

    public static void registerNullHandlers() {
        for(AbstractNullHandler nh : AnnotatedInstanceUtil.getNullHandlers()) {
            if(nullHandlers.containsKey(nh.getCapability())) {
                continue;
            }

            CompactMachines3.logger.info("Registered null handler for capability: {}", nh.getCapability().getName());
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
