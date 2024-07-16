package yesman.epicfight.api.utils;

import net.minecraft.entity.Entity;

public class EntityUtils {

    public static boolean isEntityUpsideDown(Entity entity) {
        String entityName = entity.getName().getString();
        return entityName.equalsIgnoreCase("Dinnerbone") || entityName.equalsIgnoreCase("Grumm");
    }

}
