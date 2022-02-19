package gg.moonflower.tolerablecreepers.datagen;

import gg.moonflower.pollen.api.datagen.provider.tags.PollinatedBlockTagsProvider;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.data.DataGenerator;

public class TCBlockTagProvider extends PollinatedBlockTagsProvider {

    public TCBlockTagProvider(DataGenerator generator, PollinatedModContainer container) {
        super(generator, container);
    }

    @Override
    protected void addTags() {
        super.addTags();
        this.tag(TCTags.CREEPIE_REPELLENTS);
    }
}
