package gg.moonflower.tolerablecreepers.core.forge;

import dev.architectury.platform.forge.EventBuses;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import gg.moonflower.tolerablecreepers.core.TolerableCreepersClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TolerableCreepers.MOD_ID)
public class TolerableCreepersForge {

    public TolerableCreepersForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(TolerableCreepers.MOD_ID, eventBus);
        eventBus.addListener(this::commonInit);
        eventBus.addListener(this::clientInit);

        TolerableCreepers.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TolerableCreepersClient::init);
    }

    private void commonInit(FMLCommonSetupEvent event) {
        TolerableCreepers.postInit();
    }

    private void clientInit(FMLClientSetupEvent event) {
        TolerableCreepersClient.postInit();
    }
}
