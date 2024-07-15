package yesman.epicfight.world.capabilities.entitypatch;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.utils.ExtendedDamageSource.StunType;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class HumanoidMobPatch<T extends CreatureEntity> extends MobPatch<T> {
	protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> weaponLivingMotions;
	protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> weaponAttackMotions;
	
	public HumanoidMobPatch(Faction faction) {
		super(faction);
		this.setWeaponMotions();
	}

	@Override
	protected void initAI() {
		super.initAI();

		if (this.original.getVehicle() != null && this.original.getVehicle() instanceof MobEntity) {
			this.setAIAsMounted(this.original.getVehicle());
		} else {
			this.setAIAsInfantry(this.original.getMainHandItem().getItem() instanceof ShootableItem);
		}
	}

	@Override
	public void onStartTracking(ServerPlayerEntity trackingPlayer) {
		this.modifyLivingMotionByCurrentItem();
	}

	@SuppressWarnings("unchecked")
	protected void setWeaponMotions() {
		this.weaponLivingMotions = Maps.newHashMap();
		this.weaponLivingMotions.put(WeaponCategories.GREATSWORD, ImmutableMap.of(
			CapabilityItem.Styles.TWO_HAND, Sets.newHashSet(
				Pair.of(LivingMotions.WALK, Animations.BIPED_WALK_TWOHAND),
				Pair.of(LivingMotions.CHASE, Animations.BIPED_WALK_TWOHAND)
			)
		));

		this.weaponAttackMotions = Maps.newHashMap();
	}

	protected CombatBehaviors.Builder<HumanoidMobPatch<?>> getHoldingItemWeaponMotionBuilder() {
		CapabilityItem itemCap = this.getHoldingItemCapability(Hand.MAIN_HAND);

		if (this.weaponAttackMotions.containsKey(itemCap.getWeaponCategory())) {
			Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>> motionByStyle = this.weaponAttackMotions.get(itemCap.getWeaponCategory());
			Style style = itemCap.getStyle(this);

			if (motionByStyle.containsKey(style) || motionByStyle.containsKey(CapabilityItem.Styles.COMMON)) {
				return motionByStyle.getOrDefault(style, motionByStyle.get(CapabilityItem.Styles.COMMON));
			}
		}

		return null;
	}

	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();

		if (builder != null) {
			this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, builder.build(this)));
			this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), 1.0D, true));
		}
	}
	
	public void setAIAsMounted(Entity ridingEntity) {
		if (this.isArmed()) {
			if (ridingEntity instanceof AbstractHorseEntity) {
				this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), 1.0D, true));
			}
		}
	}
	
	protected final void commonMobAnimatorInit(ClientAnimator clientAnimator) {
		clientAnimator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
		clientAnimator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
		clientAnimator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
		clientAnimator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
		clientAnimator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
		clientAnimator.setCurrentMotionsAsDefault();
	}
	
	protected final void commonAggresiveMobAnimatorInit(ClientAnimator clientAnimator) {
		clientAnimator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
		clientAnimator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
		clientAnimator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_WALK);
		clientAnimator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
		clientAnimator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
		clientAnimator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
		clientAnimator.setCurrentMotionsAsDefault();
	}

	@Override
	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, Hand hand) {
		this.initAI();

		if (hand == Hand.OFF_HAND) {
			if (!from.isEmpty()) {
				from.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::removeModifier);
			}
			if (!fromCap.isEmpty()) {
				fromCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this).get(EpicFightAttributes.ARMOR_NEGATION.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::removeModifier);
				fromCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this).get(EpicFightAttributes.IMPACT.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get())::removeModifier);
				fromCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this).get(EpicFightAttributes.MAX_STRIKES.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::removeModifier);
			}

			if (!to.isEmpty()) {
				to.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::addTransientModifier);
			}
			if (!toCap.isEmpty()) {
				toCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this).get(EpicFightAttributes.ARMOR_NEGATION.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::addTransientModifier);
				toCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this).get(EpicFightAttributes.IMPACT.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get())::addTransientModifier);
				toCap.getAttributeModifiers(EquipmentSlotType.MAINHAND, this).get(EpicFightAttributes.MAX_STRIKES.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::addTransientModifier);
			}
		}

		this.modifyLivingMotionByCurrentItem();

		super.updateHeldItem(fromCap, toCap, from, to, hand);
	}

	@Override
	public HumanoidArmature getArmature() {
		return (HumanoidArmature)this.armature;
	}

	public void modifyLivingMotionByCurrentItem() {
		this.getAnimator().resetLivingAnimations();

		CapabilityItem mainhandCap = this.getHoldingItemCapability(Hand.MAIN_HAND);
		CapabilityItem offhandCap = this.getAdvancedHoldingItemCapability(Hand.OFF_HAND);

		Map<LivingMotion, AnimationProvider<?>> motionModifier = new HashMap<>(mainhandCap.getLivingMotionModifier(this, Hand.MAIN_HAND));
		motionModifier.putAll(offhandCap.getLivingMotionModifier(this, Hand.OFF_HAND));

		for (Map.Entry<LivingMotion, AnimationProvider<?>> entry : motionModifier.entrySet()) {
			this.getAnimator().addLivingAnimation(entry.getKey(), entry.getValue().get());
		}

		if (this.weaponLivingMotions != null && this.weaponLivingMotions.containsKey(mainhandCap.getWeaponCategory())) {
			Map<Style, Set<Pair<LivingMotion, StaticAnimation>>> mapByStyle = this.weaponLivingMotions.get(mainhandCap.getWeaponCategory());
			Style style = mainhandCap.getStyle(this);

			if (mapByStyle.containsKey(style) || mapByStyle.containsKey(CapabilityItem.Styles.COMMON)) {
				Set<Pair<LivingMotion, StaticAnimation>> animModifierSet = mapByStyle.getOrDefault(style, mapByStyle.get(CapabilityItem.Styles.COMMON));

				for (Pair<LivingMotion, StaticAnimation> pair : animModifierSet) {
					this.animator.addLivingAnimation(pair.getFirst(), pair.getSecond());
				}
			}
		}

		SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
		msg.putEntries(this.getAnimator().getLivingAnimations().entrySet());
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, this.original);
	}
	
	public boolean isArmed() {
		Item heldItem = this.original.getMainHandItem().getItem();
		return heldItem instanceof SwordItem || heldItem instanceof ToolItem || heldItem instanceof TridentItem;
	}
	
	@Override
	public void onMount(boolean isMountOrDismount, Entity ridingEntity) {
		if (this.original == null) {
			return;
		}
		
		if (!this.original.level.isClientSide() && !this.original.isNoAi()) {
			Set<Goal> toRemove = Sets.newHashSet();
			this.selectGoalToRemove(toRemove);
			toRemove.forEach(this.original.goalSelector::removeGoal);
			
			if (isMountOrDismount) {
				this.setAIAsMounted(ridingEntity);
			} else {
				this.setAIAsInfantry(this.original.getMainHandItem().getItem() instanceof ShootableItem);
			}
		}
	}

	@Override
	public StaticAnimation getHitAnimation(StunType stunType) {
		if (this.original.getVehicle() != null) {
			return Animations.BIPED_HIT_ON_MOUNT;
		} else {
            return switch (stunType) {
                case LONG -> Animations.BIPED_HIT_LONG;
                case SHORT -> Animations.BIPED_HIT_SHORT;
                case HOLD -> Animations.BIPED_HIT_SHORT;
                case KNOCKDOWN -> Animations.BIPED_KNOCKDOWN;
                case NEUTRALIZE -> Animations.BIPED_COMMON_NEUTRALIZED;
                case FALL -> Animations.BIPED_LANDING;
                case NONE -> null;
            };
		}
	}
}