package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MountAttackAnimation extends AttackAnimation {

	public MountAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, Collider collider, Joint colliderJoint, String path, Armature armature) {
		super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, path, armature);
	}

	protected Vec3f getCoordVector(LivingEntityPatch<?> entitypatch) {
		return new Vec3f(0, 0, 0);
	}
}