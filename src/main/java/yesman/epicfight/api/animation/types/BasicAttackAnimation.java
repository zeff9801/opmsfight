package yesman.epicfight.api.animation.types;

import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;

public class BasicAttackAnimation extends AttackAnimation {

	boolean STIFF_COMBO_ATTACKS = true;

	public BasicAttackAnimation(float convertTime, float antic, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		this(convertTime, antic, antic, contact, recovery, collider, colliderJoint, path, armature);
	}

	public BasicAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, path, armature);
		this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
		this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
		this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
	}

	public BasicAttackAnimation(float convertTime, float antic, float contact, float recovery, Hand hand, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		super(convertTime, antic, antic, contact, recovery, hand, collider, colliderJoint, path, armature);
		this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
		this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
		this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
	}

	public BasicAttackAnimation(float convertTime, String path, Armature armature, Phase... phases) {
		super(convertTime, path, armature, phases);
		this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
		this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
		this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
	}

	@Override
	protected void bindPhaseState(Phase phase) {
		float preDelay = phase.preDelay;

		this.stateSpectrumBlueprint
				.newTimePair(phase.start, preDelay)
				.addState(EntityState.PHASE_LEVEL, 1)
				.newTimePair(phase.start, phase.contact)
				.addState(EntityState.CAN_SKILL_EXECUTION, false)
				.newTimePair(phase.start, phase.recovery)
				.addState(EntityState.MOVEMENT_LOCKED, true)
				.addState(EntityState.UPDATE_LIVING_MOTION, false)
				.addState(EntityState.CAN_BASIC_ATTACK, false)
				.newTimePair(phase.start, phase.end)
				.addState(EntityState.INACTION, true)
				.newTimePair(preDelay, phase.contact)
				.addState(EntityState.ATTACKING, true)
				.addState(EntityState.PHASE_LEVEL, 2)
				.newTimePair(phase.contact, phase.end)
				.addState(EntityState.PHASE_LEVEL, 3)
				.addState(EntityState.TURNING_LOCKED, true);
	}

	@Override
	public void postInit() {
		super.postInit();

		if (!this.properties.containsKey(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED)) {
			float basisSpeed = Float.parseFloat(String.format(Locale.US, "%.2f", (1.0F / this.getTotalTime())));
			this.addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, basisSpeed);
		}
	}

	@Override
	public void end(LivingEntityPatch<?> entitypatch, DynamicAnimation nextAnimation, boolean isEnd) {
		super.end(entitypatch, nextAnimation, isEnd);

		if (!isEnd && !nextAnimation.isMainFrameAnimation() && entitypatch.isLogicalClient() && !STIFF_COMBO_ATTACKS) {
			float playbackSpeed = EpicFightOptions.A_TICK * this.getPlaySpeed(entitypatch, this);
			entitypatch.getClientAnimator().baseLayer.copyLayerTo(entitypatch.getClientAnimator().baseLayer.getLayer(Layer.Priority.HIGHEST), playbackSpeed);
		}
	}


	@Override
	protected TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, DynamicAnimation animation, float time) {
		TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap = super.getStatesMap(entitypatch, animation, time);

		if (!STIFF_COMBO_ATTACKS){
			stateMap.put(EntityState.MOVEMENT_LOCKED, (Object)false);
			stateMap.put(EntityState.UPDATE_LIVING_MOTION, (Object)true);
		}

		return stateMap;
	}

	@Override
	protected Vector3d getCoordVector(LivingEntityPatch<?> entitypatch, DynamicAnimation dynamicAnimation) {
		Vector3d vec3 = super.getCoordVector(entitypatch, dynamicAnimation);

		if (entitypatch.shouldBlockMoving() && this.getProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE).orElse(false)) {
			vec3 = vec3.scale(0.0F);
		}

		return vec3;
	}

	@Override
	public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
		if (entitypatch.isLogicalClient()) {
			if (entitypatch.getClientAnimator().getPriorityFor(this) == Layer.Priority.HIGHEST) {
				return Optional.of(JointMaskEntry.BASIC_ATTACK_MASK);
			}
		}

		return super.getJointMaskEntry(entitypatch, useCurrentMotion);
	}

	@Override
	public boolean isBasicAttackAnimation() {
		return true;
	}

	@Override
	public boolean shouldPlayerMove(LocalPlayerPatch playerpatch) {
		if (playerpatch.isLogicalClient()) {
			if (!STIFF_COMBO_ATTACKS) {
				if (playerpatch.getOriginal().input.forwardImpulse != 0.0F || playerpatch.getOriginal().input.leftImpulse != 0.0F) {
					return false;
				}
			}
		}

		return true;
	}
}
