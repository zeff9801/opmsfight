package yesman.epicfight.api.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.types.StaticAnimation;

public interface ExtendedDamageSource {
	public static EpicFightDamageSource causePlayerDamage(PlayerEntity player, StunType stunType, StaticAnimation animation, Hand hand) {
        return new EpicFightDamageSource("player", player, stunType, animation, hand);
    }
	
	public static EpicFightDamageSource causeMobDamage(LivingEntity mob, StunType stunType, StaticAnimation animation) {
        return new EpicFightDamageSource("mob", mob, stunType, animation);
    }
	
	public static EpicFightDamageSource causeDamage(String msg, LivingEntity attacker, StunType stunType, StaticAnimation animation) {
        return new EpicFightDamageSource(msg, attacker, stunType, animation);
    }
	
	public void setImpact(float amount);
	public void setArmorNegation(float amount);
	public void setStunType(StunType stunType);
	public void setFinisher(boolean flag);
	public void setInitialPosition(Vector3d initialPosition);
	public float getImpact();
	public float getArmorNegation();
	public boolean isBasicAttack();
	public boolean isFinisher();
	public int getAnimationId();
	public StunType getStunType();
	public Entity getOwner();
	public String getType();


	public enum StunType {
		NONE("damage_source.epicfight.stun_none", true),
		SHORT("damage_source.epicfight.stun_short", false),
		LONG("damage_source.epicfight.stun_long", true),
		HOLD("damage_source.epicfight.stun_hold", false),
		KNOCKDOWN("damage_source.epicfight.stun_knockdown", true),
		NEUTRALIZE("damage_source.epicfight.stun_neutralize", true),
		FALL("damage_source.epicfight.stun_fall", true);

		private final String tooltip;
		private final boolean fixedStunTime;

		StunType(String tooltip, boolean fixedStunTime) {
			this.tooltip = tooltip;
			this.fixedStunTime = fixedStunTime;
		}

		public boolean hasFixedStunTime() {
			return this.fixedStunTime;
		}

		@Override
		public String toString() {
			return this.tooltip;
		}
	}
}