
package yesman.epicfight.skill;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.BasicAttackEvent;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillConsumeEvent;

import java.util.List;
import java.util.UUID;

public class BasicAttack extends Skill {
	private static final UUID EVENT_UUID = UUID.fromString("a42e0198-fdbc-11eb-9a03-0242ac130003");

	public static Skill.Builder<BasicAttack> createBasicAttackBuilder() {
		return (new Builder<BasicAttack>()).setCategory(SkillCategories.BASIC_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
	}

	public static void setComboCounterWithEvent(ComboCounterHandleEvent.Causal reason, ServerPlayerPatch playerpatch, SkillContainer container, StaticAnimation causalAnimation, int value) {
		int prevValue = container.getDataManager().getDataValue(SkillDataKeys.COMBO_COUNTER.get());
		ComboCounterHandleEvent comboResetEvent = new ComboCounterHandleEvent(reason, playerpatch, causalAnimation, prevValue, value);
		container.getExecuter().getEventListener().triggerEvents(EventType.COMBO_COUNTER_HANDLE_EVENT, comboResetEvent);
		container.getDataManager().setData(SkillDataKeys.COMBO_COUNTER.get(), comboResetEvent.getNextValue());
	}

	public BasicAttack(Builder<? extends Skill> builder) {
		super(builder);
	}

	@Override
	public void onInitiate(SkillContainer container) {
		container.getExecuter().getEventListener().addEventListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID, (event) -> {
			if (!event.getAnimation().isBasicAttackAnimation() && event.getAnimation().getProperty(ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER).orElse(true)) {
				CapabilityItem item = event.getPlayerPatch().getHoldingItemCapability(Hand.MAIN_HAND);

				if (item.shouldCancelCombo(event.getPlayerPatch())) {
					setComboCounterWithEvent(ComboCounterHandleEvent.Causal.ACTION_ANIMATION_RESET, event.getPlayerPatch(), container, event.getAnimation(), 0);
				}
			}
		});
	}

	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecuter().getEventListener().removeListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID);
	}

	@Override
	public boolean isExecutableState(PlayerPatch<?> executer) {
		EntityState playerState = executer.getEntityState();
		PlayerEntity player = executer.getOriginal();

		return !(player.isSpectator() || executer.footsOnGround() || !playerState.canBasicAttack());
	}

	@Override
	public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
		SkillConsumeEvent event = new SkillConsumeEvent(executer, this, this.resource);
		executer.getEventListener().triggerEvents(EventType.SKILL_CONSUME_EVENT, event);

		if (!event.isCanceled()) {
			event.getResourceType().consumer.consume(this, executer, event.getAmount());
		}

		if (executer.getEventListener().triggerEvents(EventType.BASIC_ATTACK_EVENT, new BasicAttackEvent(executer))) {
			return;
		}

		CapabilityItem cap = executer.getHoldingItemCapability(Hand.MAIN_HAND);
		StaticAnimation attackMotion = null;
		ServerPlayerEntity player = executer.getOriginal();
		SkillContainer skillContainer = executer.getSkill(this);
		SkillDataManager dataManager = skillContainer.getDataManager();
		int comboCounter = dataManager.getDataValue(SkillDataKeys.COMBO_COUNTER.get());

		if (player.isPassenger()) {
			Entity entity = player.getVehicle();

			if ((entity instanceof IJumpingMount ridable && ridable.canJump()) && cap.availableOnHorse() && cap.getMountAttackMotion() != null) {
				comboCounter %= cap.getMountAttackMotion().size();
				attackMotion = cap.getMountAttackMotion().get(comboCounter).get();
				comboCounter++;
			}
		} else {
			List<AnimationProvider<?>> combo = cap.getAutoAttckMotion(executer);
			int comboSize = combo.size();
			boolean dashAttack = player.isSprinting();

			if (dashAttack) {
				comboCounter = comboSize - 2;
			} else {
				comboCounter %= comboSize - 2;
			}

			attackMotion = combo.get(comboCounter).get();
			comboCounter = dashAttack ? 0 : comboCounter + 1;
		}

		setComboCounterWithEvent(ComboCounterHandleEvent.Causal.ACTION_ANIMATION_RESET, executer, skillContainer, attackMotion, comboCounter);

		if (attackMotion != null) {
			executer.playAnimationSynchronized(attackMotion, 0);
		}

		executer.updateEntityState();
	}

	@Override
	public void updateContainer(SkillContainer container) {
		if (!container.getExecuter().isLogicalClient() && container.getExecuter().getTickSinceLastAction() > 16 && container.getDataManager().getDataValue(SkillDataKeys.COMBO_COUNTER.get()) > 0) {
			setComboCounterWithEvent(ComboCounterHandleEvent.Causal.TIME_EXPIRED_RESET, (ServerPlayerPatch)container.getExecuter(), container, null, 0);
		}
	}
}
