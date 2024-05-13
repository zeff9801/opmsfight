package yesman.epicfight.world.capabilities.entitypatch;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.EpicFightDamageSource;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSetAttackTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public abstract class MobPatch<T extends MobEntity> extends LivingEntityPatch<T> {
	protected final Faction mobFaction;
	
	public MobPatch() {
		this.mobFaction = Faction.NEUTRAL;
	}
	
	public MobPatch(Faction faction) {
		this.mobFaction = faction;
	}
	
	@Override
	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		
		if (!entityIn.level.isClientSide() && !this.original.isNoAi()) {
			this.initAI();
		}
	}
	
	protected void initAI() {
		Set<Goal> toRemove = Sets.newHashSet();
		this.selectGoalToRemove(toRemove);
		toRemove.forEach(this.original.goalSelector::removeGoal);
	}
	
	protected void selectGoalToRemove(Set<Goal> toRemove) {
		for (PrioritizedGoal wrappedGoal : this.original.goalSelector.availableGoals) {
			Goal goal = wrappedGoal.getGoal();
			
			if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof RangedAttackGoal || goal instanceof TargetChasingGoal) {
				toRemove.add(goal);
			}
		}
	}
	
	protected final void commonMobUpdateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.IDLE;
		} else {
			if (original.getVehicle() != null)
				currentLivingMotion = LivingMotions.MOUNT;
			else
				if (this.original.getDeltaMovement().y < -0.55F)
					currentLivingMotion = LivingMotions.FALL;
				else if (original.animationSpeed > 0.01F)
					currentLivingMotion = LivingMotions.WALK;
				else
					currentLivingMotion = LivingMotions.IDLE;
		}
		
		this.currentCompositeMotion = this.currentLivingMotion;
	}
	
	protected final void commonAggressiveMobUpdateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.IDLE;
		} else {
			if (original.getVehicle() != null) {
				currentLivingMotion = LivingMotions.MOUNT;
			} else {
				if (this.original.getDeltaMovement().y < -0.55F)
					currentLivingMotion = LivingMotions.FALL;
				else if (original.animationSpeed > 0.01F)
					if (original.isAggressive())
						currentLivingMotion = LivingMotions.CHASE;
					else
						currentLivingMotion = LivingMotions.WALK;
				else
					currentLivingMotion = LivingMotions.IDLE;
			}
		}
		
		this.currentCompositeMotion = this.currentLivingMotion;
	}
	
	protected final void commonAggressiveRangedMobUpdateMotion(boolean considerInaction) {
		this.commonAggressiveMobUpdateMotion(considerInaction);
		UseAction useAction = this.original.getItemInHand(this.original.getUsedItemHand()).getUseAnimation();
		
		if (this.original.isUsingItem()) {
			if (useAction == UseAction.CROSSBOW)
				currentCompositeMotion = LivingMotions.RELOAD;
			else
				currentCompositeMotion = LivingMotions.AIM;
		} else {
			if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
		}
		
		if (CrossbowItem.isCharged(this.original.getMainHandItem()))
			currentCompositeMotion = LivingMotions.AIM;
		else if (this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM)
			this.playReboundAnimation();
	}
	
	@Override
	public void updateArmor(CapabilityItem fromCap, CapabilityItem toCap, EquipmentSlotType slotType) {
		if (this.original.getAttributes().hasAttribute(EpicFightAttributes.STUN_ARMOR.get())) {
			if (fromCap != null) {
				this.original.getAttributes().removeAttributeModifiers(fromCap.getAttributeModifiers(slotType, this));
			}
			
			if (toCap != null) {
				this.original.getAttributes().addTransientAttributeModifiers(toCap.getAttributeModifiers(slotType, this));
			}
		}
	}
	
	@Override
	public boolean isTeammate(Entity entityIn) {
		EntityPatch<?> cap = entityIn.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		
		if (cap != null && cap instanceof MobPatch) {
			if (((MobPatch<?>) cap).mobFaction.equals(this.mobFaction)) {
				Optional<LivingEntity> opt = Optional.ofNullable(this.getTarget());
				return opt.map((attackTarget) -> !attackTarget.is(entityIn)).orElse(true);
			}
		}
		
		return super.isTeammate(entityIn);
	}
	@Override
	public AttackResult attack(EpicFightDamageSource damageSource, Entity target, Hand hand) {
		boolean shouldSwap = hand == Hand.OFF_HAND;

		this.epicFightDamageSource = damageSource;
		//this.setOffhandDamage(shouldSwap);
		this.original.doHurtTarget(target);
		//this.recoverMainhandDamage(shouldSwap);
		this.epicFightDamageSource = null;

		return super.attack(damageSource, target, hand);
	}
	@Override
	public LivingEntity getTarget() {
		return this.original.getTarget();
	}
	
	public void setAttakTargetSync(LivingEntity entityIn) {
		if (!this.original.level.isClientSide()) {
			this.original.setTarget(entityIn);
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSetAttackTarget(this.original.getId(), entityIn != null ? entityIn.getId() : -1), this.original);
		}
	}
	
	@Override
	public float getAttackDirectionPitch() {
		Entity attackTarget = this.getTarget();
		if (attackTarget != null) {
			float partialTicks = EpicFightMod.isPhysicalClient() ? Minecraft.getInstance().getFrameTime() : 1.0F;
			Vector3d target = attackTarget.getEyePosition(partialTicks);
			Vector3d vector3d = this.original.getEyePosition(partialTicks);
			double d0 = target.x - vector3d.x;
			double d1 = target.y - vector3d.y;
			double d2 = target.z - vector3d.z;
			double d3 = (double) Math.sqrt(d0 * d0 + d2 * d2);
			return MathHelper.clamp(MathHelper.wrapDegrees((float) ((MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)))), -30.0F, 30.0F);
		} else {
			return super.getAttackDirectionPitch();
		}
	}
}