package gg.moonflower.tolerablecreepers.core;

import gg.moonflower.pollen.api.event.events.world.ExplosionEvents;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;

public class TolerableCreepers {
    public static final String MOD_ID = "tolerablecreepers";
    public static final Platform PLATFORM = Platform.builder(MOD_ID)
            .clientInit(TolerableCreepers::onClientInit)
            .clientPostInit(TolerableCreepers::onClientPostInit)
            .commonInit(TolerableCreepers::onCommonInit)
            .commonPostInit(TolerableCreepers::onCommonPostInit)
            .build();

    public static void onClientInit() {
    }

    public static void onClientPostInit(Platform.ModSetupContext ctx) {
    }

    public static void onCommonInit() {
        TCItems.ITEMS.register(TolerableCreepers.PLATFORM);
        ExplosionEvents.DETONATE.register(TCEvents::onExplosionDetonate);
    }

    public static void onCommonPostInit(Platform.ModSetupContext ctx) {
    }
}
