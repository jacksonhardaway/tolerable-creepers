package gg.moonflower.tolerablecreepers.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.List;

public class TCEvents {
    public static void onExplosionDetonate(Level level, Explosion explosion, List<Entity> entityList) {
        if (explosion.getSourceMob() instanceof Creeper) {
            explosion.getToBlow().clear();
            //TODO spawn spore cloud
        }
    }
}
