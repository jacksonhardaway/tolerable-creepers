package gg.moonflower.tolerablecreepers.datagen;

import gg.moonflower.pollen.api.datagen.provider.PollinatedLanguageProvider;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCItems;
import net.minecraft.data.DataGenerator;

public class TCLanguageProvider extends PollinatedLanguageProvider {

    public TCLanguageProvider(DataGenerator generator, PollinatedModContainer container) {
        super(generator, container, "en_us");
    }

    @Override
    protected void registerTranslations() {
        this.addItem(TCItems.CREEPER_SPORES, "Creeper Spores");
        this.addEntityType(TCEntities.CREEPER_SPORES, "Creeper Spores");
        this.addEntityType(TCEntities.CREEPIE, "Creepie");
    }
}
