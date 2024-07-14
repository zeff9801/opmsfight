package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.model.Armature;

public class LongHitAnimation extends ActionAnimation {
	public LongHitAnimation(float convertTime, String path, Armature armature) {
		this(convertTime, path, armature, false);
	}

	public LongHitAnimation(float convertTime, String path, Armature armature, boolean noRegister) {
		super(convertTime, path, armature, noRegister);
		this.addProperty(AnimationProperty.MoveCoordFunctions.STOP_MOVEMENT, true);
		this.addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);

		this.stateSpectrumBlueprint.clear()
				.newTimePair(0.0F, Float.MAX_VALUE)
				.addState(EntityState.TURNING_LOCKED, true)
				.addState(EntityState.MOVEMENT_LOCKED, true)
				.addState(EntityState.UPDATE_LIVING_MOTION, false)
				.addState(EntityState.CAN_BASIC_ATTACK, false)
				.addState(EntityState.CAN_SKILL_EXECUTION, false)
				.addState(EntityState.INACTION, true)
				.addState(EntityState.HURT_LEVEL, 2);
	}
}