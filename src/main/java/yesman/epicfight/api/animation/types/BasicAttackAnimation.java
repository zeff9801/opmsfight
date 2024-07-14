package yesman.epicfight.api.animation.types;

import net.minecraft.util.Hand;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.MoveCoordFunctions;
import yesman.epicfight.api.client.animation.JointMaskEntry;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;

public class BasicAttackAnimation extends AttackAnimation {
	public BasicAttackAnimation(float convertTime, float antic, float contact, float recovery, @Nullable Collider collider, String index, String path, Model model) {
		this(convertTime, antic, antic, contact, recovery, collider, index, path, model);
	}

	public BasicAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, String index, String path, Model model) {
		super(convertTime, antic, preDelay, contact, recovery, collider, index, path, model);

		this.addProperty(AttackAnimationProperty.ROTATE_X, true);
		this.addProperty(MoveCoordFunctions.CANCELABLE_MOVE, true);
	}

	public BasicAttackAnimation(float convertTime, float antic, float contact, float recovery, Hand hand, @Nullable Collider collider, String index, String path, Model model) {
		super(convertTime, antic, antic, contact, recovery, hand, collider, index, path, model);

		this.addProperty(AttackAnimationProperty.ROTATE_X, true);
		this.addProperty(MoveCoordFunctions.CANCELABLE_MOVE, true);
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
	protected Vec3f getCoordVector(LivingEntityPatch<?> entitypatch, DynamicAnimation dynamicAnimation) {
		Vec3f vec3 = super.getCoordVector(entitypatch, dynamicAnimation);
		
		if (entitypatch.shouldBlockMoving() && this.getProperty(MoveCoordFunctions.CANCELABLE_MOVE).orElse(false)) {
			vec3.scale(0.0F);
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
