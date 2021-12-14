package gg.moonflower.tolerablecreepers.core.fabric;

import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.fabricmc.api.ModInitializer;

public class TolerableCreepersFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TolerableCreepers.PLATFORM.setup();
    }
}
