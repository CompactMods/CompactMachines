package org.dave.compactmachines3.world.data.provider;

import net.minecraft.tileentity.TileEntity;
import org.dave.compactmachines3.utility.AnnotatedInstanceUtil;
import org.dave.compactmachines3.utility.Logz;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExtraTileDataProviderRegistry {
    private static List<AbstractExtraTileDataProvider> extraTileData = new ArrayList<>();

    public static void registerExtraTileDataProviders() {
        for(AbstractExtraTileDataProvider etdp : AnnotatedInstanceUtil.getExtraTileDataProviders()) {
            if(etdp.getName() == null) {
                continue;
            }

            Logz.info("Registered extra tile data provider '%s'", etdp.getName());
            extraTileData.add(etdp);
        }
    }

    public static boolean hasDataProvider(TileEntity tileEntity) {
        return extraTileData.stream().anyMatch(provider -> provider.worksWith(tileEntity));
    }

    public static List<AbstractExtraTileDataProvider> getDataProviders(TileEntity tileEntity) {
        return extraTileData.stream().filter(provider -> provider.worksWith(tileEntity)).collect(Collectors.toList());
    }
}
