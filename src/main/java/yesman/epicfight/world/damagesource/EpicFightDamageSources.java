package yesman.epicfight.world.damagesource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;

public class EpicFightDamageSources {

    public enum TYPE {
        EXECUTION("execution"),
        SHOCK_WAVE("shock_wave"),
        WITHER_BEAM("wither_beam"),
        TRIDENT("mob"),
        MOB_ATTACK("mob"),
        PLAYER_ATTACK("player"),
        ENDER_DRAGON_BREATH("enderdragon_breath"),
        VANILLA_GENERIC(""),
        FINISHER("finisher"),
        COUNTER("counter"),
        WEAPON_INNATE("weapon_innate"),
        GUARD_PUNCTURE("guard_puncture"),
        PARTIAL_DAMAGE("partial_damage");


        public String identifierName;

        TYPE(String identifierName) {
            this.identifierName = identifierName;
        }
    }

    public static EpicFightDamageSource copy(DamageSource source) {
        return new EpicFightDamageSource(source);
    }

    public static EpicFightDamageSource execution(LivingEntity owner) {
        return new EpicFightDamageSource(TYPE.EXECUTION, owner, owner);
    }

    public static EpicFightDamageSource shockwave(LivingEntity owner) {
        return new EpicFightDamageSource(TYPE.SHOCK_WAVE, owner, owner);
    }

    public static EpicFightDamageSource witherBeam(LivingEntity owner) {
        return new EpicFightDamageSource(TYPE.WITHER_BEAM, owner, owner);
    }

    public static EpicFightDamageSource trident(Entity owner, Entity causingEntity) {
        return new EpicFightDamageSource(TYPE.TRIDENT, owner, causingEntity);
    }

    public static EpicFightDamageSource mobAttack(LivingEntity owner) {
        return new EpicFightDamageSource(TYPE.MOB_ATTACK, owner);
    }

    public static EpicFightDamageSource playerAttack(PlayerEntity owner) {
        return new EpicFightDamageSource(TYPE.PLAYER_ATTACK, owner);
    }

    public static EpicFightDamageSource enderDragonBreath(LivingEntity owner, Entity causingEntity) {
        return new EpicFightDamageSource(TYPE.ENDER_DRAGON_BREATH, owner, owner);
    }
}