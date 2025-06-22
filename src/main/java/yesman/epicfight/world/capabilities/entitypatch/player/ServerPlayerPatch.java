package yesman.epicfight.world.capabilities.entitypatch.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.*;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.DodgeSuccessEvent;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SetTargetEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerPlayerPatch extends PlayerPatch<ServerPlayerEntity> {
	private LivingEntity attackTarget;
	private boolean updatedMotionCurrentTick;
	
	@Override
	public void onJoinWorld(ServerPlayerEntity player, EntityJoinWorldEvent event) {
		super.onJoinWorld(player, event);

		CapabilitySkill skillCapability = this.getSkillCapability();

		for (SkillContainer skill : skillCapability.skillContainers) {
			if (skill.getSkill() != null && skill.getSkill().getCategory().shouldSynchronize()) {
				EpicFightNetworkManager.sendToPlayer(new SPChangeSkill(skill.getSlot(), skill.getSkill().toString(), SPChangeSkill.State.ENABLE), this.original);
			}
		}

		List<String> learnedSkill = Lists.newArrayList();

		for (SkillCategory category : SkillCategory.ENUM_MANAGER.universalValues()) {
			if (skillCapability.hasCategory(category)) {
				learnedSkill.addAll(Lists.newArrayList(skillCapability.getLearnedSkills(category).stream().map((skill) -> skill.toString()).iterator()));
			}
		}

		this.eventListeners.addEventListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, PLAYER_EVENT_UUID, (playerevent) -> {
			if (playerevent.getDamageSource().isBasicAttack()) {
				SkillContainer container = this.getSkill(SkillSlots.WEAPON_INNATE);
				ItemStack mainHandItem = this.getOriginal().getMainHandItem();

				if (!container.isFull() && !container.isActivated() && container.hasSkill(EpicFightCapabilities.getItemStackCapability(mainHandItem).getInnateSkill(this, mainHandItem))) {
					float value = container.getResource() + playerevent.getAttackDamage();

					if (value > 0.0F) {
						this.getSkill(SkillSlots.WEAPON_INNATE).getSkill().setConsumptionSynchronize(this, value);
					}
				}
			}
		}, 10);

		EpicFightNetworkManager.sendToPlayer(new SPAddLearnedSkill(learnedSkill.toArray(new String[0])), this.original);
		EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setPlayerMode(this.getOriginal().getId(), this.playerMode), this.original);
	}
	
	@Override
	public void onStartTracking(ServerPlayerEntity trackingPlayer) {
		SPChangeLivingMotion msg = new SPChangeLivingMotion(this.getOriginal().getId());
		msg.putEntries(this.getAnimator().getLivingAnimations().entrySet());

		for (SkillContainer container : this.getSkillCapability().skillContainers) {
			for (SkillDataKey<?> key : container.getDataManager().keySet()) {
				if (key.syncronizeTrackingPlayers()) {
					EpicFightNetworkManager.sendToPlayer(
							new SPAddOrRemoveSkillData(key, container.getSlot().universalOrdinal(), container.getDataManager().getDataValue(key), SPAddOrRemoveSkillData.AddRemove.ADD, this.original.getId()),
							trackingPlayer);
				}
			}
		}

		EpicFightNetworkManager.sendToPlayer(msg, trackingPlayer);
		EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setPlayerMode(this.getOriginal().getId(), this.playerMode), trackingPlayer);
	}

	@Override
	public void tick(LivingUpdateEvent event) {
		super.tick(event);
		this.updatedMotionCurrentTick = false;
	}

	@Override
	public void updateMotion(boolean considerInaction) {
	}

	@Override
	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, Hand hand) {
		if (this.isChargingSkill()) {
			Skill skill = this.chargingSkill.asSkill();
			skill.cancelOnServer(this, null);
			this.resetSkillCharging();

			EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.expired(this.getSkill(skill).getSlotId()), this.original);
		}


		CapabilityItem mainHandCap = (hand == Hand.MAIN_HAND) ? toCap : this.getHoldingItemCapability(Hand.MAIN_HAND);
		mainHandCap.changeWeaponInnateSkill(this, (hand == Hand.MAIN_HAND) ? to : this.original.getMainHandItem());

		if (hand == Hand.OFF_HAND) {
			if (!from.isEmpty()) {
				Multimap<Attribute, AttributeModifier> modifiers = from.getAttributeModifiers(EquipmentSlotType.MAINHAND);
				modifiers.get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::removeModifier);
			}
			if (!fromCap.isEmpty()) {
				Multimap<Attribute, AttributeModifier> modifiers = fromCap.getAllAttributeModifiers(EquipmentSlotType.MAINHAND);
				modifiers.get(EpicFightAttributes.ARMOR_NEGATION.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::removeModifier);
				modifiers.get(EpicFightAttributes.IMPACT.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get())::removeModifier);
				modifiers.get(EpicFightAttributes.MAX_STRIKES.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::removeModifier);
				modifiers.get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::removeModifier);
			}

			if (!to.isEmpty()) {
				Multimap<Attribute, AttributeModifier> modifiers = to.getAttributeModifiers(EquipmentSlotType.MAINHAND);
				modifiers.get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::addTransientModifier);
			}
			if (!toCap.isEmpty()) {
				Multimap<Attribute, AttributeModifier> modifiers = toCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this);
				modifiers.get(EpicFightAttributes.ARMOR_NEGATION.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::addTransientModifier);
				modifiers.get(EpicFightAttributes.IMPACT.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get())::addTransientModifier);
				modifiers.get(EpicFightAttributes.MAX_STRIKES.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::addTransientModifier);
				modifiers.get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::addTransientModifier);
			}
		}

		this.modifyLivingMotionByCurrentItem();

		super.updateHeldItem(fromCap, toCap, from, to, hand);
	}

	public void modifyLivingMotionByCurrentItem() {
		if (this.updatedMotionCurrentTick) {
			return;
		}

		Map<LivingMotion, StaticAnimation> oldLivingAnimations = this.getAnimator().getLivingAnimations();
		Map<LivingMotion, StaticAnimation> newLivingAnimations = Maps.newHashMap();

		CapabilityItem mainhandCap = this.getHoldingItemCapability(Hand.MAIN_HAND);
		CapabilityItem offhandCap = this.getAdvancedHoldingItemCapability(Hand.OFF_HAND);

		Map<LivingMotion, AnimationProvider<?>> livingMotionModifiers = new HashMap<>(mainhandCap.getLivingMotionModifier(this, Hand.MAIN_HAND));
		livingMotionModifiers.putAll(offhandCap.getLivingMotionModifier(this, Hand.OFF_HAND));

		for (Map.Entry<LivingMotion, AnimationProvider<?>> entry : livingMotionModifiers.entrySet()) {
			StaticAnimation aniamtion = entry.getValue().get();

			if (!oldLivingAnimations.containsKey(entry.getKey())) {
				this.updatedMotionCurrentTick = true;
			} else if (oldLivingAnimations.get(entry.getKey()) != aniamtion) {
				this.updatedMotionCurrentTick = true;
			}

			newLivingAnimations.put(entry.getKey(), aniamtion);
		}

		for (LivingMotion oldLivingMotion : oldLivingAnimations.keySet()) {
			if (!newLivingAnimations.containsKey(oldLivingMotion)) {
				this.updatedMotionCurrentTick = true;
				break;
			}
		}

		if (this.updatedMotionCurrentTick) {
			this.getAnimator().resetLivingAnimations();
			newLivingAnimations.forEach(this.getAnimator()::addLivingAnimation);

			SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
			msg.putEntries(newLivingAnimations.entrySet());

			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(msg, this.original);
		}
	}


	@Override
	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier, AnimationPacketProvider packetProvider) {
		super.playAnimationSynchronized(animation, convertTimeModifier, packetProvider);
		EpicFightNetworkManager.sendToPlayer(packetProvider.get(animation, convertTimeModifier, this), this.original);
	}

	@Override
	public void reserveAnimation(StaticAnimation animation) {
		super.reserveAnimation(animation);
		EpicFightNetworkManager.sendToPlayer(new SPPlayAnimation(animation, this.original.getId(), 0.0F), this.original);
	}

	@Override
	public void setModelYRot(float amount, boolean sendPacket) {
		super.setModelYRot(amount, sendPacket);

		if (sendPacket) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.setPlayerYRot(this.original.getId(), this.modelYRot), this.original);
		}
	}

	@Override
	public void disableModelYRot(boolean sendPacket) {
		super.disableModelYRot(sendPacket);

		if (sendPacket) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.disablePlayerYRot(this.original.getId()), this.original);
		}
	}

	@Override
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		HurtEvent.Pre hurtEvent = new HurtEvent.Pre(this, damageSource, amount);

		if (this.getEventListener().triggerEvents(EventType.HURT_EVENT_PRE, hurtEvent)) {
			return new AttackResult(hurtEvent.getResult(), hurtEvent.getAmount());
		} else {
			return super.tryHurt(damageSource, amount);
		}
	}

	@Override
	public void onDodgeSuccess(DamageSource damageSource) {
		super.onDodgeSuccess(damageSource);

		DodgeSuccessEvent dodgeSuccessEvent = new DodgeSuccessEvent(this, damageSource);
		this.getEventListener().triggerEvents(EventType.DODGE_SUCCESS_EVENT, dodgeSuccessEvent);
	}

	@Override
	public void toMiningMode(boolean synchronize) {
		super.toMiningMode(synchronize);

		if (synchronize) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.setPlayerMode(this.original.getId(), PlayerMode.MINING), this.original);
		}
	}

	@Override
	public void toBattleMode(boolean synchronize) {
		super.toBattleMode(synchronize);

		if (synchronize) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(SPModifyPlayerData.setPlayerMode(this.original.getId(), PlayerMode.BATTLE), this.original);
		}
	}

	@Override
	public boolean isTeammate(Entity entityIn) {
		if (entityIn instanceof PlayerEntity && !this.getOriginal().server.isPvpAllowed()) {
			return true;
		}

		return super.isTeammate(entityIn);
	}

	@Override
	public void setLastAttackSuccess(boolean setter) {
		if (setter) {
			EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setLastAttackResult(this.original.getId(), true), this.original);
		}

		this.isLastAttackSuccess = setter;
	}

	public void setAttackTarget(LivingEntity entity) {
		SetTargetEvent setTargetEvent = new SetTargetEvent(this, entity);
		this.getEventListener().triggerEvents(EventType.SET_TARGET_EVENT, setTargetEvent);

		this.attackTarget = setTargetEvent.getTarget();
	}

	@Override
	public void startSkillCharging(ChargeableSkill chargingSkill) {
		super.startSkillCharging(chargingSkill);
		EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.chargingBegin(this.getSkill((Skill)chargingSkill).getSlotId()), this.getOriginal());
	}

	@Override
	public LivingEntity getTarget() {
		return this.attackTarget;
	}

	@Override
	public void setGrapplingTarget(LivingEntity grapplingTarget) {
		super.setGrapplingTarget(grapplingTarget);
		EpicFightNetworkManager.sendToPlayer(SPModifyPlayerData.setGrapplingTarget(this.original.getId(), grapplingTarget), this.original);
	}
	

}