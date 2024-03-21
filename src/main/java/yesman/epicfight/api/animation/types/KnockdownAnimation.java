package yesman.epicfight.api.animation.types;

import net.minecraft.util.EntityDamageSource;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.EpicFightDamageSource;
import yesman.epicfight.api.utils.ExtendedDamageSource;

public class KnockdownAnimation extends LongHitAnimation {
	public KnockdownAnimation(float convertTime, float delayTime, String path, Model model) {
		super(convertTime, path, model);

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