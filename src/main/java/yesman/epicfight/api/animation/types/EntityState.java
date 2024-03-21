package yesman.epicfight.api.animation.types;

import java.util.function.Function;

import net.minecraft.util.DamageSource;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;

public class EntityState {
	public static class StateFactor<T> implements TypeFlexibleHashMap.TypeKey<T> {
		private final String name;
		private final T defaultValue;

		public StateFactor(String name, T defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}

		public String toString() {
			return this.name;
		}

		public T defaultValue() {
			return this.defaultValue;
		}
	}

	public static final EntityState DEFAULT_STATE = new EntityState(new TypeFlexibleHashMap<>(true));

	public static final StateFactor<Boolean> TURNING_LOCKED = new StateFactor<>("turningLocked", false);
	public static final StateFactor<Boolean> MOVEMENT_LOCKED = new StateFactor<>("movementLocked", false);
	public static final StateFactor<Boolean> ATTACKING = new StateFactor<>("attacking", false);
	public static final StateFactor<Boolean> CAN_BASIC_ATTACK = new StateFactor<>("canBasicAttack", true);
	public static final StateFactor<Boolean> CAN_SKILL_EXECUTION = new StateFactor<>("canExecuteSkill", true);
	public static final StateFactor<Boolean> INACTION = new StateFactor<>("inaction", false);
	public static final StateFactor<Boolean> KNOCKDOWN = new StateFactor<>("knockdown", false);
	public static final StateFactor<Boolean> LOCKON_ROTATE = new StateFactor<>("lockonRotate", false);
	public static final StateFactor<Boolean> UPDATE_LIVING_MOTION = new StateFactor<>("updateLivingMotion", true);
	public static final StateFactor<Integer> HURT_LEVEL = new StateFactor<>("hurtLevel", 0);
	public static final StateFactor<Integer> PHASE_LEVEL = new StateFactor<>("phaseLevel", 0);
	public static final StateFactor<Function<DamageSource, AttackResult.ResultType>> ATTACK_RESULT = new StateFactor<>("attackResultModifier", (damagesource) -> AttackResult.ResultType.SUCCESS);

	TypeFlexibleHashMap<StateFactor<?>> stateMap;

	public EntityState(TypeFlexibleHashMap<StateFactor<?>> states) {
		this.stateMap = states;
	}

	public <T> void setState(StateFactor<T> stateFactor, T val) {
		this.stateMap.put(stateFactor, (Object)val);
	}

	public <T> T getState(StateFactor<T> stateFactor) {
		return this.stateMap.getOrDefault(stateFactor);
	}

	public boolean turningLocked() {
		return this.getState(EntityState.TURNING_LOCKED);
	}

	public boolean movementLocked() {
		return this.getState(EntityState.MOVEMENT_LOCKED);
	}

	public boolean attacking() {
		return this.getState(EntityState.ATTACKING);
	}

	public AttackResult.ResultType attackResult(DamageSource damagesource) {
		return this.getState(EntityState.ATTACK_RESULT).apply(damagesource);
	}

	public boolean canBasicAttack() {
		return this.getState(EntityState.CAN_BASIC_ATTACK);
	}

	public boolean canUseSkill() {
		return this.getState(EntityState.CAN_SKILL_EXECUTION);
	}

	public boolean inaction() {
		return this.getState(EntityState.INACTION);
	}

	public boolean updateLivingMotion() {
		return this.getState(EntityState.UPDATE_LIVING_MOTION);
	}

	public boolean hurt() {
		return this.getState(EntityState.HURT_LEVEL) > 0;
	}

	public int hurtLevel() {
		return this.getState(EntityState.HURT_LEVEL);
	}

	public boolean knockDown() {
		return this.getState(EntityState.KNOCKDOWN);
	}

	public boolean lockonRotate() {
		return this.getState(EntityState.LOCKON_ROTATE);
	}

	/**
	 * 1: anticipation
	 * 2: attacking
	 * 3: recovery
	 * @return level
	 */
	public int getLevel() {
		return this.getState(EntityState.PHASE_LEVEL);
	}
}