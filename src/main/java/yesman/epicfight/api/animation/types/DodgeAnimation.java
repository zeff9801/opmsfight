package yesman.epicfight.api.animation.types;

import java.util.function.Function;
import net.minecraft.entity.EntitySize;
import net.minecraft.util.DamageSource;
import yesman.epicfight.api.animation.property.AnimationProperty.MoveCoordFunctions;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPRotatePlayerYaw;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class DodgeAnimation extends ActionAnimation {
	private final EntitySize size;

	public DodgeAnimation(float convertTime, String path, float width, float height, Model model) {
		this(convertTime, 0.0F, path, width, height, model);
	}
	public static final Function<DamageSource, AttackResult.ResultType> DODGEABLE_SOURCE_VALIDATOR = (damagesource) -> {
		if (damagesource.getEntity() != null && !damagesource.isExplosion()
				&& !damagesource.isMagic() && !damagesource.isBypassArmor()
				&& !damagesource.isBypassInvul()) {

			return AttackResult.ResultType.MISSED;
		}

		return AttackResult.ResultType.SUCCESS;
	};

	public DodgeAnimation(float convertTime, float delayTime, String path, float width, float height, Model model) {
		super(convertTime, delayTime, path, model);

		if (width > 0.0F || height > 0.0F) {
			this.size = EntitySize.scalable(width, height);
		} else {
			this.size = null;
		}

		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, 10.0F)
			.addState(EntityState.TURNING_LOCKED, true)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.CAN_BASIC_ATTACK, false)
			.addState(EntityState.CAN_SKILL_EXECUTION, false)
			.addState(EntityState.INACTION, true)
			.newTimePair(delayTime, Float.MAX_VALUE)
				.addState(EntityState.ATTACK_RESULT, DODGEABLE_SOURCE_VALIDATOR);


		this.addProperty(MoveCoordFunctions.AFFECT_SPEED, true);
	}

	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);

		if (this.size != null) {
			entitypatch.resetSize(this.size);
		}
	}

	@Override
	public void end(LivingEntityPatch<?> entitypatch, boolean isEnd) {
		super.end(entitypatch, isEnd);

		if (this.size != null) {
			entitypatch.getOriginal().refreshDimensions();
		}

		if (entitypatch.isLogicalClient() && entitypatch instanceof LocalPlayerPatch) {
			((LocalPlayerPatch)entitypatch).changeYaw(0);
			EpicFightNetworkManager.sendToServer(new CPRotatePlayerYaw(0));
		}
	}
}