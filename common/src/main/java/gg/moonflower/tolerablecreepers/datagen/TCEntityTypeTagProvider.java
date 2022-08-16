package gg.moonflower.tolerablecreepers.datagen;

import gg.moonflower.pollen.api.datagen.provider.tags.PollinatedEntityTypeTagsProvider;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.tolerablecreepers.core.registry.TCEntities;
import gg.moonflower.tolerablecreepers.core.registry.TCTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class TCEntityTypeTagProvider extends PollinatedEntityTypeTagsProvider {

    public TCEntityTypeTagProvider(DataGenerator generator, PollinatedModContainer container) {
        super(generator, container);
    }

    @Override
    protected void addTags() {
        super.addTags();
        this.tag(TCTags.EXPLOSION_IMMUNE).add(EntityType.ARMOR_STAND, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, EntityType.PAINTING, EntityType.MINECART, EntityType.BOAT).addOptional(new ResourceLocation("mannequins", "mannequin"));
        this.tag(TCTags.EXPLOSION_PRONE);
        this.tag(TCTags.CREEPIE_AVOID).add(EntityType.OCELOT, EntityType.CAT);
        this.tag(TCTags.CREEPIE_FRIEND).add(EntityType.CREEPER, TCEntities.CREEPIE.get()); // Creepies are not considered alone if there are creepies or creepers nearby
    }
}
