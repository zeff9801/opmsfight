package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.EpicFightDamageSource;

public class KnockdownAnimation extends LongHitAnimation {
	public KnockdownAnimation(float convertTime, String path, Armature armature) {
		this(convertTime, path, armature, false);
	}
	public KnockdownAnimation(float convertTime, String path, Armature armature, boolean noRegister) {
		super(convertTime, path, armature, noRegister);
		this.stateSpectrumBlueprint
				.addState(EntityState.KNOCKDOWN, true)
				.addState(EntityState.ATTACK_RESULT, (damagesource) -> {
					if (damagesource.getEntity() != null && !damagesource.isExplosion() && !damagesource.isMagic() && !damagesource.isBypassInvul()) {
						if (damagesource instanceof EpicFightDamageSource) {
							return ((EpicFightDamageSource)damagesource).isFinisher() ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.BLOCKED;
						} else {
							return AttackResult.ResultType.BLOCKED;
						}
					}

					return AttackResult.ResultType.SUCCESS;
				});
	}
}