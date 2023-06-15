package gg.moonflower.tolerablecreepers.core.fabric;

import gg.moonflower.tolerablecreepers.core.TolerableCreepersClient;
import net.fabricmc.api.ClientModInitializer;

public class TolerableCreepersFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TolerableCreepersClient.init();
        TolerableCreepersClient.postInit();
//        TolerableCreepers.PLATFORM.setup();
    }
}
