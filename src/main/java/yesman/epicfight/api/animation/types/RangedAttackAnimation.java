package yesman.epicfight.api.animation.types;

import net.minecraft.entity.IRangedAttackMob;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RangedAttackAnimation extends AttackAnimation {
	public RangedAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, Collider collider, Joint colliderJoint, String path, Armature armature) {
		super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, path, armature);
	}

	@Override
	public void hurtCollidingEntities(LivingEntityPatch<?> entitypatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, Phase phase) {
		if (state.attacking() && entitypatch.getTarget() != null && (entitypatch.getOriginal() instanceof IRangedAttackMob)) {
			((IRangedAttackMob)entitypatch.getOriginal()).performRangedAttack(entitypatch.getTarget(), elapsedTime);
		}
	}
}