
package yesman.epicfight.skill;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class AirAttack extends Skill {
	public static Skill.Builder<AirAttack> createAirAttackBuilder() {
		return new Skill.Builder<AirAttack>().setCategory(SkillCategories.AIR_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.STAMINA);
	}

	public AirAttack(Builder<? extends Skill> builder) {
		super(builder);
	}

	@Override
	public boolean isExecutableState(PlayerPatch<?> executer) {
		EntityState playerState = executer.getEntityState();
		PlayerEntity player = executer.getOriginal();
		return !(player.isPassenger() || player.isSpectator() || executer.footsOnGround() || !playerState.canBasicAttack());
	}

	@Override
	public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
		List<AnimationProvider<?>> motions = executer.getHoldingItemCapability(Hand.MAIN_HAND).getAutoAttckMotion(executer);
		StaticAnimation attackMotion = motions.get(motions.size() - 1).get();

		if (attackMotion != null) {
			super.executeOnServer(executer, args);
			executer.playAnimationSynchronized(attackMotion, 0);
		}
	}
}
