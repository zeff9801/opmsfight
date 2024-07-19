package yesman.epicfight.api.utils;

import net.minecraft.util.DamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;

public class DamageSourceHelper {

    public static boolean is(DamageSource source, EpicFightDamageSources.TYPE type) {
        return source.msgId.equals(type.identifierName);
    }

}
