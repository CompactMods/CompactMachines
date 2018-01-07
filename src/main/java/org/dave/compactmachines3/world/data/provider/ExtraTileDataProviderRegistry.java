package org.dave.compactmachines3.world.data.provider;

import org.dave.compactmachines3.utility.AnnotatedInstanceUtil;
import org.dave.compactmachines3.utility.Logz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtraTileDataProviderRegistry {
    private static HashMap<Class, List<AbstractExtraTileDataProvider>> extraTileData = new HashMap<>();

    public static void registerExtraTileDataProviders() {
        for(AbstractExtraTileDataProvider etdp : AnnotatedInstanceUtil.getExtraTileDataProviders()) {
            if(etdp.getApplicableClass() == null) {
                continue;
            }

            if(!extraTileData.containsKey(etdp.getApplicableClass())) {
                extraTileData.put(etdp.getApplicableClass(), new ArrayList<>());
            }

            List<AbstractExtraTileDataProvider> existingProviders = extraTileData.get(etdp.getApplicableClass());
            if(!existingProviders.contains(etdp)) {
                Logz.info("Registered extra tile data provider for class: %s", etdp.getApplicableClass().getCanonicalName());
                existingProviders.add(etdp);
            }
        }
    }

    public static boolean hasDataProvider(Class clz) {
        return extraTileData.containsKey(clz);
    }

    public static List<AbstractExtraTileDataProvider> getDataProviders(Class clz) {
        return extraTileData.get(clz);
    }
}
