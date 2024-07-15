package yesman.epicfight.api.animation.types;

import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;

import yesman.epicfight.api.client.animation.JointMaskEntry;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;

public class BasicAttackAnimation extends AttackAnimation {
	public BasicAttackAnimation(float convertTime, float antic, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		this(convertTime, antic, antic, contact, recovery, collider, colliderJoint, path, armature);
	}

	public BasicAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
		super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, path, armature);
		this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
		this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
		this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
	}

	public BasicAttackAnimation(float convertTime, float antic, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, Joint colliderJoint, String path, Armature armature) {
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
	public void setLinkAnimation(Pose pose1, float timeModifier, LivingEntityPatch<?> entitypatch, LinkAnimation dest) {
		float extTime = Math.max(this.convertTime + timeModifier, 0);
		
		if (entitypatch instanceof PlayerPatch<?>) {
			PlayerPatch<?> playerpatch = (PlayerPatch<?>)entitypatch;
			Phase phase = this.getPhaseByTime(playerpatch.getAnimator().getPlayerFor(this).getElapsedTime());
			extTime *= (float)(this.totalTime * playerpatch.getAttackSpeed(phase.hand));
		}
		
		extTime = Math.max(extTime - this.convertTime, 0);
		super.setLinkAnimation(pose1, extTime, entitypatch, dest);
	}
	
	@Override
	protected void onLoaded() {
		super.onLoaded();
		
		if (!this.properties.containsKey(AttackAnimationProperty.BASIS_ATTACK_SPEED)) {
			float basisSpeed = Float.parseFloat(String.format(Locale.US, "%.2f", (1.0F / this.totalTime)));
			this.addProperty(AttackAnimationProperty.BASIS_ATTACK_SPEED, basisSpeed);
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
	boolean STIFF_COMBO_ATTACKS = false;
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
}
