package yesman.epicfight.world.damagesource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EpicFightDamageSources {

    public static EpicFightDamageSource copy(DamageSource damageSource) {
        return new EpicFightDamageSource(damageSource);
    }

    public static EpicFightDamageSource shockwave(LivingEntity owner) {
        return new EpicFightDamageSource("shock_wave", owner, owner);
    }

    public static EpicFightDamageSource witherBeam(LivingEntity owner) {
        return new EpicFightDamageSource("wither_beam", owner, owner);
    }

    public static EpicFightDamageSource trident(Entity owner, Entity causingEntity) {
        return new EpicFightDamageSource("mob", owner, causingEntity);
    }

    public static EpicFightDamageSource mobAttack(LivingEntity owner) {
        return new EpicFightDamageSource("mob", owner);
    }

    public static EpicFightDamageSource playerAttack(PlayerEntity owner) {
        return new EpicFightDamageSource("player", owner);
    }

    public static EpicFightDamageSource enderDragonBreath(LivingEntity owner, Entity causingEntity) {
        return new EpicFightDamageSource("enderdragon_breath", owner, owner);
    }
}