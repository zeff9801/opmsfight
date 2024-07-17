package yesman.epicfight.world.capabilities.entitypatch;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.SupplementedTask;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.brain.BrainRecomposer;
import yesman.epicfight.world.entity.ai.brain.task.AnimatedCombatBehavior;
import yesman.epicfight.world.entity.ai.brain.task.BackUpIfTooCloseStopInaction;
import yesman.epicfight.world.entity.ai.brain.task.MoveToTargetSinkStopInaction;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class CustomHumanoidMobPatch<T extends CreatureEntity> extends HumanoidMobPatch<T> {
	private final MobPatchReloadListener.CustomHumanoidMobPatchProvider provider;

	public CustomHumanoidMobPatch(Faction faction, MobPatchReloadListener.CustomHumanoidMobPatchProvider provider) {
		super(faction);
		this.provider = provider;
		this.weaponLivingMotions = this.provider.getHumanoidWeaponMotions();
		this.weaponAttackMotions = this.provider.getHumanoidCombatBehaviors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		boolean isUsingBrain = !this.getOriginal().getBrain().availableBehaviorsByPriority.isEmpty();

		if (isUsingBrain) {
			if (!holdingRanedWeapon) {
				CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

				if (builder != null) {
					BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, AttackTargetTask.class, new AnimatedCombatBehavior<>(this, builder.build(this)));
				}

				BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.FIGHT, SupplementedTask.class, new SupplementedTask<>((entity) -> entity.isHolding(is -> is.getItem() instanceof CrossbowItem), new BackUpIfTooCloseStopInaction<>(5, 0.75F)));
				BrainRecomposer.replaceBehaviors((Brain<T>)this.original.getBrain(), Activity.CORE, WalkToTargetTask.class, new MoveToTargetSinkStopInaction());
			}
		} else {
			if (!holdingRanedWeapon) {
				CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

				if (builder != null) {
					this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, builder.build(this)));
					this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), this.provider.getChasingSpeed(), true));
				}
			}
		}
	}

	@Override
	protected void setWeaponMotions() {
		if (this.weaponAttackMotions == null) {
			super.setWeaponMotions();
		}
	}

	@Override
	protected void initAttributes() {
		EntitySize dimension = this.original.getDimensions(Pose.STANDING);
		this.original.getAttribute(EpicFightAttributes.WEIGHT.get()).setBaseValue(dimension.width * dimension.height * WEIGHT_CORRECTION);
		this.original.getAttribute(EpicFightAttributes.MAX_STRIKES.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.MAX_STRIKES.get()));
		this.original.getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.ARMOR_NEGATION.get()));
		this.original.getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.IMPACT.get()));
		this.original.getAttribute(EpicFightAttributes.STUN_ARMOR.get()).setBaseValue(this.provider.getAttributeValues().get(EpicFightAttributes.STUN_ARMOR.get()));

		if (this.provider.getAttributeValues().containsKey(Attributes.ATTACK_DAMAGE)) {
			this.original.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.provider.getAttributeValues().get(Attributes.ATTACK_DAMAGE));
		}
	}
	@Override
	public void initAnimator(Animator clientAnimator) {
		for (Pair<LivingMotion, StaticAnimation> pair : this.provider.getDefaultAnimations()) {
			clientAnimator.addLivingAnimation(pair.getFirst(), pair.getSecond());
		}
	}

	@Override
	public void updateMotion(boolean considerInaction) {
		super.commonAggressiveMobUpdateMotion(considerInaction);

		if (this.original.isUsingItem()) {
			CapabilityItem activeItem = this.getHoldingItemCapability(this.original.getUsedItemHand());
			UseAction useAnim = this.original.getItemInHand(this.original.getUsedItemHand()).getUseAnimation();
			UseAction secondUseAnim = activeItem.getUseAnimation(this);

			if (useAnim == UseAction.BLOCK || secondUseAnim == UseAction.BLOCK)
				if (activeItem.getWeaponCategory() == WeaponCategories.SHIELD)
					currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
				else
					currentCompositeMotion = LivingMotions.BLOCK;
			else if (useAnim == UseAction.BOW || useAnim == UseAction.SPEAR)
				currentCompositeMotion = LivingMotions.AIM;
			else if (useAnim == UseAction.CROSSBOW)
				currentCompositeMotion = LivingMotions.RELOAD;
			else
				currentCompositeMotion = currentLivingMotion;
		} else {
			if (CrossbowItem.isCharged(this.original.getMainHandItem()))
				currentCompositeMotion = LivingMotions.AIM;
			else if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
			else if (this.original.swinging && this.original.getSleepingPos().isEmpty())
				currentCompositeMotion = LivingMotions.DIGGING;
			else
				currentCompositeMotion = currentLivingMotion;

			if (this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM) {
				this.playReboundAnimation();
			}
		}
	}

	@Override
	public StaticAnimation getHitAnimation(StunType stunType) {
		return this.provider.getStunAnimations().get(stunType);
	}

	@Override
	public SoundEvent getWeaponHitSound(Hand hand) {
		CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);

		if (itemCap.isEmpty()) {
			return this.provider.getHitSound();
		}

		return itemCap.getHitSound();
	}

	@Override
	public SoundEvent getSwingSound(Hand hand) {
		CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);

		if (itemCap.isEmpty()) {
			return this.provider.getSwingSound();
		}

		return itemCap.getSmashingSound();
	}

	@Override
	public HitParticleType getWeaponHitParticle(Hand hand) {
		CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);

		if (itemCap.isEmpty()) {
			return this.provider.getHitParticle();
		}

		return itemCap.getHitParticle();
	}

	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float scale = this.provider.getScale();
		return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
	}
}