package yesman.epicfight.world.capabilities.entitypatch;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.api.utils.ExtendedDamageSource;
import yesman.epicfight.api.utils.ExtendedDamageSource.StunType;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPPlayAnimation;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class LivingEntityPatch<T extends LivingEntity> extends HurtableEntityPatch<T> {
	public static final DataParameter<Float> STUN_SHIELD = new DataParameter<Float>(251, DataSerializers.FLOAT);
	public static final DataParameter<Float> MAX_STUN_SHIELD = new DataParameter<Float>(252, DataSerializers.FLOAT);
	public static final DataParameter<Integer> EXECUTION_RESISTANCE = new DataParameter<Integer>(254, DataSerializers.INT);
	public static final DataParameter<Boolean> AIRBORNE = new DataParameter<Boolean>(250, DataSerializers.BOOLEAN);
	protected static final double WEIGHT_CORRECTION = 37.037D;

	private AttackResult.ResultType lastResultType;
	private float lastDealDamage;
	protected Entity lastTryHurtEntity;
	protected LivingEntity grapplingTarget;
	protected Armature armature;
	protected EntityState state = EntityState.DEFAULT_STATE;
	protected Animator animator;
	protected Vector3d lastAttackPosition;
	protected EpicFightDamageSource epicFightDamageSource;
	protected boolean isLastAttackSuccess;

	public LivingMotion currentLivingMotion = LivingMotions.IDLE;
	public LivingMotion currentCompositeMotion = LivingMotions.IDLE;


	@Override
	public void onConstructed(T entityIn) {
		super.onConstructed(entityIn);

		this.armature = Armatures.getArmatureFor(this);
		this.animator = EpicFightMod.getAnimator(this);
		this.animator.init();
		this.original.getEntityData().define(STUN_SHIELD, Float.valueOf(0.0F));
		this.original.getEntityData().define(MAX_STUN_SHIELD, Float.valueOf(0.0F));
		this.original.getEntityData().define(EXECUTION_RESISTANCE, Integer.valueOf(0));
		this.original.getEntityData().define(AIRBORNE, Boolean.valueOf(false));
	}

	@Override
	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		this.initAttributes();
	}

	public abstract void initAnimator(Animator clientAnimator);

	public abstract void updateMotion(boolean considerInaction);

	public Armature getArmature() {
		return this.armature;
	}

	protected void initAttributes() {
		EntitySize dimension = this.original.getDimensions(net.minecraft.entity.Pose.STANDING);
		this.original.getAttribute(EpicFightAttributes.WEIGHT.get()).setBaseValue(dimension.width * dimension.height * WEIGHT_CORRECTION);
		this.original.getAttribute(EpicFightAttributes.MAX_STRIKES.get()).setBaseValue(1.0D);
		this.original.getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).setBaseValue(0.0D);
		this.original.getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(0.5D);
	}

	@Override
	public void tick(LivingUpdateEvent event) {
		if (this.original.getHealth() <= 0.0F) {
			this.original.xRot = (0);

			AnimationPlayer animPlayer = this.getAnimator().getPlayerFor(null);

			if (this.original.deathTime >= 19 && !animPlayer.isEmpty() && !animPlayer.isEnd()) {
				this.original.deathTime--;
			}
		}

		this.animator.tick();
		super.tick(event);

		if (this.original.deathTime == 19) {
			this.aboutToDeath();
		}

		if (!this.getEntityState().inaction() && this.original.isOnGround() && this.isAirborneState()) {
			this.setAirborneState(false);
		}
	}

	public void poseTick(DynamicAnimation animation, Pose pose, float elapsedTime, float partialTicks) {
		if (pose.getJointTransformData().containsKey("Head")) {
			if (animation.doesHeadRotFollowEntityHead()) {
				float headRotO = this.original.yBodyRotO - this.original.yHeadRotO;
				float headRot = this.original.yBodyRot - this.original.yHeadRot;
				float partialHeadRot = MathUtils.lerpBetween(headRotO, headRot, partialTicks);
				OpenMatrix4f toOriginalRotation = new OpenMatrix4f(this.armature.getBindedTransformFor(pose, this.armature.searchJointByName("Head"))).removeScale().removeTranslation().invert();
				Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
				Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);
				OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(-this.original.xRot, xAxis).mulFront(OpenMatrix4f.createRotatorDeg(partialHeadRot, yAxis));
				pose.getOrDefaultTransform("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
			}
		}
	}

	public void onFall(LivingFallEvent event) {
		if (!this.getOriginal().level.isClientSide() && this.isAirborneState()) {
			StaticAnimation fallAnimation = this.getAnimator().getLivingAnimation(LivingMotions.LANDING_RECOVERY, this.getHitAnimation(StunType.FALL));

			if (fallAnimation != null) {
				this.playAnimationSynchronized(fallAnimation, 0);
			}
		}

		this.setAirborneState(false);
	}

	@Override
	public void onDeath(LivingDeathEvent event) {
		this.getAnimator().playDeathAnimation();
		this.currentLivingMotion = LivingMotions.DEATH;
	}

	public void updateEntityState() {
		this.state = this.animator.getEntityState();
	}

	public void cancelAnyAction() {
		this.original.stopUsingItem();
		ForgeEventFactory.onUseItemStop(this.original, this.original.getUseItem(), this.original.getUseItemRemainingTicks());
	}

	public CapabilityItem getHoldingItemCapability(Hand hand) {
		return EpicFightCapabilities.getItemStackCapability(this.original.getItemInHand(hand));
	}

	/**
	 * Returns an empty capability if the item in mainhand is incompatible with the item in offhand
	 */
	public CapabilityItem getAdvancedHoldingItemCapability(Hand hand) {
		if (hand == Hand.MAIN_HAND) {
			return getHoldingItemCapability(hand);
		} else {
			return this.isOffhandItemValid() ? this.getHoldingItemCapability(hand) : CapabilityItem.EMPTY;
		}
	}

	public EpicFightDamageSource getDamageSource(StaticAnimation animation, Hand hand) {
		EpicFightDamageSource damagesource = EpicFightDamageSources.mobAttack(this.original).setAnimation(animation);
		damagesource.setImpact(this.getImpact(hand));
		damagesource.setArmorNegation(this.getArmorNegation(hand));
		damagesource.setHurtItem(this.original.getItemInHand(hand));
		return damagesource;
	}


	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		return AttackResult.of(this.getEntityState().attackResult(damageSource), amount);
	}

	public AttackResult tryHarm(Entity target, EpicFightDamageSource damagesource, float amount) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
		AttackResult result = (entitypatch != null) ? entitypatch.tryHurt(damagesource, amount) : AttackResult.success(amount);

		return result;
	}

	@Nullable
	public EpicFightDamageSource getEpicFightDamageSource() {
		return this.epicFightDamageSource;
	}

	/**
	 * Swap item and attributes of mainhand for offhand item and attributes
	 * You must call {@link LivingEntityPatch#recoverMainhandDamage} method again after finishing the damaging process.
	 */
	protected void setOffhandDamage(Hand hand, Collection<AttributeModifier> mainhandAttributes, Collection<AttributeModifier> offhandAttributes) {
		if (hand == Hand.MAIN_HAND) {
			return;
		}

		/**
		 * Swap hand items
		 */
		ItemStack mainHandItem = this.getOriginal().getMainHandItem();
		ItemStack offHandItem = this.getOriginal().getOffhandItem();
		this.getOriginal().setItemInHand(Hand.MAIN_HAND, offHandItem);
		this.getOriginal().setItemInHand(Hand.OFF_HAND, mainHandItem);

		/**
		 * Swap item's attributes before {@link LivingEntity#setItemInHand} called
		 */
		ModifiableAttributeInstance damageAttributeInstance = this.original.getAttribute(Attributes.ATTACK_DAMAGE);
		mainhandAttributes.forEach(damageAttributeInstance::removeModifier);
		offhandAttributes.forEach(damageAttributeInstance::addTransientModifier);
	}

	/**
	 * Set mainhand item's attribute modifiers
	 */
	protected void recoverMainhandDamage(Hand hand, Collection<AttributeModifier> mainhandAttributes, Collection<AttributeModifier> offhandAttributes) {
		if (hand == Hand.MAIN_HAND) {
			return;
		}

		ItemStack mainHandItem = this.getOriginal().getMainHandItem();
		ItemStack offHandItem = this.getOriginal().getOffhandItem();
		this.getOriginal().setItemInHand(Hand.MAIN_HAND, offHandItem);
		this.getOriginal().setItemInHand(Hand.OFF_HAND, mainHandItem);

		ModifiableAttributeInstance damageAttributeInstance = this.original.getAttribute(Attributes.ATTACK_DAMAGE);
		offhandAttributes.forEach(damageAttributeInstance::removeModifier);
		mainhandAttributes.forEach(damageAttributeInstance::addTransientModifier);
	}

	public void setLastAttackResult(AttackResult attackResult) {
		this.lastResultType = attackResult.resultType;
		this.lastDealDamage = attackResult.damage;
	}

	public void setLastAttackEntity(Entity tryHurtEntity) {
		this.lastTryHurtEntity = tryHurtEntity;
	}

	protected boolean checkLastAttackSuccess(Entity target) {
		boolean success = target.is(this.lastTryHurtEntity);
		this.lastTryHurtEntity = null;

		if (success && !this.isLastAttackSuccess) {
			this.setLastAttackSuccess(true);
		}

		return success;
	}

	public AttackResult attack(EpicFightDamageSource damageSource, Entity target, Hand hand) {
		return this.checkLastAttackSuccess(target) ? new AttackResult(this.lastResultType, this.lastDealDamage) : AttackResult.missed(0.0F);
	}

	public float getModifiedBaseDamage(float baseDamage) {
		return baseDamage;
	}

	public boolean onDrop(LivingDropsEvent event) {
		return false;
	}


	public float getStunShield() {
		return this.original.getEntityData().get(STUN_SHIELD).floatValue();
	}

	public void setStunShield(float value) {
		value = Math.max(value, 0);
		value = Math.min(value, this.getMaxStunShield());
		this.original.getEntityData().set(STUN_SHIELD, value);
	}

	public float getMaxStunShield() {
		return this.original.getEntityData().get(MAX_STUN_SHIELD).floatValue();
	}

	public void setMaxStunShield(float value) {
		value = Math.max(value, 0);
		this.original.getEntityData().set(MAX_STUN_SHIELD, value);
	}

	public int getExecutionResistance() {
		return this.original.getEntityData().get(EXECUTION_RESISTANCE).intValue();
	}


	//TODO Implement once we port ExecutionSkills
//	public void setExecutionResistance(int value) {
//		int maxExecutionResistance = (int)this.original.getAttributeValue(EpicFightAttributes.MAX_EXECUTION_RESISTANCE.get());
//		value = Math.min(maxExecutionResistance, value);
//		this.original.getEntityData().set(EXECUTION_RESISTANCE, value);
//	}

	public float getWeight() {
		return (float) this.original.getAttributeValue(EpicFightAttributes.WEIGHT.get());
	}

	public void rotateTo(float degree, float limit, boolean syncPrevRot) {
		LivingEntity entity = this.getOriginal();
		float yRot = MathHelper.wrapDegrees(entity.yRot);
		float amount = MathHelper.clamp(MathHelper.wrapDegrees(degree - yRot), -limit, limit);
		float f1 = yRot + amount;

		if (syncPrevRot) {
			entity.yRotO = f1;
			entity.yHeadRotO = f1;
			entity.yBodyRotO = f1;
		}

		entity.yRot = f1;
		entity.yHeadRot = f1;
		entity.yBodyRot = f1;
	}

	public void rotateTo(Entity target, float limit, boolean syncPrevRot) {
		Vector3d playerPosition = this.original.position();
		Vector3d targetPosition = target.position();
		float yaw = (float) MathUtils.getYRotOfVector(targetPosition.subtract(playerPosition));
		this.rotateTo(yaw, limit, syncPrevRot);
	}

	public LivingEntity getTarget() {
		return this.original.getLastHurtMob();
	}

	public float getAttackDirectionPitch() {
		float partialTicks = EpicFightMod.isPhysicalClient() ? Minecraft.getInstance().getFrameTime() : 1.0F;
		float pitch = -this.getOriginal().getViewXRot(partialTicks);
		float correct = (pitch > 0) ? 0.03333F * (float) Math.pow(pitch, 2) : -0.03333F * (float) Math.pow(pitch, 2);

		return MathHelper.clamp(correct, -30.0F, 30.0F);
	}

	public float getCameraXRot() {
		return MathHelper.wrapDegrees(this.original.xRot);
	}

	public float getCameraYRot() {
		return MathHelper.wrapDegrees(this.original.yRot);
	}

	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float prevYRot;
		float yRot;
		float scale = this.original.isBaby() ? 0.5F : 1.0F;

		if (this.original.getVehicle() instanceof LivingEntity) {
			LivingEntity ridingEntity = (LivingEntity) this.original.getVehicle();
			prevYRot = ridingEntity.yBodyRotO;
			yRot = ridingEntity.yBodyRot;
		} else {
			prevYRot = this.isLogicalClient() ? this.original.yBodyRotO : this.original.yRot;
			yRot = this.isLogicalClient() ? this.original.yBodyRot : this.original.yRot;
		}

		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, prevYRot, yRot, partialTicks, scale, scale, scale);
	}

	public void reserveAnimation(StaticAnimation animation) {
		this.animator.reserveAnimation(animation);
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPPlayAnimation(animation, this.original.getId(), 0.0F), this.original);
	}

	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier) {
		this.playAnimationSynchronized(animation, convertTimeModifier, SPPlayAnimation::new);
	}

	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier, AnimationPacketProvider packetProvider) {
		this.animator.playAnimation(animation, convertTimeModifier);
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(packetProvider.get(animation, convertTimeModifier, this), this.original);
	}

	@FunctionalInterface
	public static interface AnimationPacketProvider {
		public SPPlayAnimation get(StaticAnimation animation, float convertTimeModifier, LivingEntityPatch<?> entitypatch);
	}

	protected void playReboundAnimation() {
		this.getClientAnimator().playReboundAnimation();
	}

	public void resetSize(EntitySize size) {
		EntitySize entitysize = this.original.dimensions;
		EntitySize entitysize1 = size;
		this.original.dimensions = entitysize1;

		if (entitysize1.width < entitysize.width) {
			double d0 = (double) entitysize1.width / 2.0D;
			this.original.setBoundingBox(new AxisAlignedBB(original.getX() - d0, original.getY(), original.getZ() - d0, original.getX() + d0,
					original.getY() + (double) entitysize1.height, original.getZ() + d0));
		} else {
			AxisAlignedBB axisalignedbb = this.original.getBoundingBox();
			this.original.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double) entitysize1.width,
					axisalignedbb.minY + (double) entitysize1.height, axisalignedbb.minZ + (double) entitysize1.width));

			if (entitysize1.width > entitysize.width && !original.level.isClientSide()) {
				float f = entitysize.width - entitysize1.width;
				this.original.move(MoverType.SELF, new Vector3d((double) f, 0.0D, (double) f));
			}
		}
	}

	@Override
	public boolean applyStun(StunType stunType, float stunTime) {
		this.original.xxa = 0.0F;
		this.original.yya = 0.0F;
		this.original.zza = 0.0F;
		this.original.setDeltaMovement(0.0D, 0.0D, 0.0D);
		this.cancelKnockback = true;

		StaticAnimation hitAnimation = this.getHitAnimation(stunType);

		if (hitAnimation != null) {
			this.playAnimationSynchronized(hitAnimation, stunType.hasFixedStunTime() ? 0.0F : stunTime);
			return true;
		}

		return false;
	}

	public void correctRotation() {
	}

	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, Hand hand) {
	}

	public void updateArmor(CapabilityItem fromCap, CapabilityItem toCap, EquipmentSlotType slotType) {
	}

	public void onAttackBlocked(HurtEvent.Pre hurtEvent, LivingEntityPatch<?> opponent) {
	}

	public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> opponent) {
	}

	public void onMount(boolean isMountOrDismount, Entity ridingEntity) {
	}

	public void notifyGrapplingWarning() {
	}

	public void onDodgeSuccess(DamageSource damageSource) {
	}

	@Override
	public boolean isStunned() {
		return this.getEntityState().hurt();
	}

	@SuppressWarnings("unchecked")
	public <A extends Animator> A getAnimator() {
		return (A) this.animator;
	}


	@OnlyIn(Dist.CLIENT)
	public ClientAnimator getClientAnimator() {
		return this.getAnimator();
	}

	public ServerAnimator getServerAnimator() {
		return this.getAnimator();
	}

	public abstract StaticAnimation getHitAnimation(StunType stunType);

	public void aboutToDeath() {
	}

	public SoundEvent getWeaponHitSound(Hand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getHitSound();
	}

	public SoundEvent getSwingSound(Hand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getSmashingSound();
	}

	public HitParticleType getWeaponHitParticle(Hand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getHitParticle();
	}

	public Collider getColliderMatching(Hand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getWeaponCollider();
	}

	public int getMaxStrikes(Hand hand) {
		return (int) (hand == Hand.MAIN_HAND ? this.original.getAttributeValue(EpicFightAttributes.MAX_STRIKES.get()) :
				this.isOffhandItemValid() ? this.original.getAttributeValue(EpicFightAttributes.OFFHAND_MAX_STRIKES.get()) : this.original.getAttribute(EpicFightAttributes.MAX_STRIKES.get()).getBaseValue());
	}

	public float getArmorNegation(Hand hand) {
		return (float) (hand == Hand.MAIN_HAND ? this.original.getAttributeValue(EpicFightAttributes.ARMOR_NEGATION.get()) :
				this.isOffhandItemValid() ? this.original.getAttributeValue(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get()) : this.original.getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).getBaseValue());
	}

	public float getImpact(Hand hand) {
		float impact;
		int i = 0;

		if (hand == Hand.MAIN_HAND) {
			impact = (float) this.original.getAttributeValue(EpicFightAttributes.IMPACT.get());
			i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, this.getOriginal().getMainHandItem());
		} else {
			if (this.isOffhandItemValid()) {
				impact = (float) this.original.getAttributeValue(EpicFightAttributes.OFFHAND_IMPACT.get());
				i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, this.getOriginal().getOffhandItem());
			} else {
				impact = (float) this.original.getAttribute(EpicFightAttributes.IMPACT.get()).getBaseValue();
			}
		}

		return impact * (1.0F + i * 0.12F);
	}

	public ItemStack getValidItemInHand(Hand hand) {
		if (hand == Hand.MAIN_HAND) {
			return this.original.getItemInHand(hand);
		} else {
			return this.isOffhandItemValid() ? this.original.getItemInHand(hand) : ItemStack.EMPTY;
		}
	}

	public boolean isOffhandItemValid() {
		return this.getHoldingItemCapability(Hand.MAIN_HAND).checkOffhandValid(this);
	}

	public boolean isTeammate(Entity entityIn) {
		if (this.original.getVehicle() != null && this.original.getVehicle().equals(entityIn)) {
			return true;
		} else if (this.isRideOrBeingRidden(entityIn)) {
			return true;
		}

		return this.original.isAlliedTo(entityIn) && this.original.getTeam() != null && !this.original.getTeam().isAllowFriendlyFire();
	}

	public boolean canPush(Entity entity) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

		if (entitypatch != null) {
			EntityState state = entitypatch.getEntityState();

			if (state.inaction()) {
				return false;
			}
		}

		EntityState thisState = this.getEntityState();

		return !thisState.inaction() && !entity.is(this.grapplingTarget);
	}

	public LivingEntity getGrapplingTarget() {
		return this.grapplingTarget;
	}

	public void setGrapplingTarget(LivingEntity grapplingTarget) {
		this.grapplingTarget = grapplingTarget;
	}

	public Vector3d getLastAttackPosition() {
		return this.lastAttackPosition;
	}

	public void setLastAttackPosition() {
		this.lastAttackPosition = this.original.position();
	}

	private boolean isRideOrBeingRidden(Entity entityIn) {
		LivingEntity orgEntity = this.getOriginal();
		for (Entity passanger : orgEntity.getPassengers()) {
			if (passanger.equals(entityIn)) {
				return true;
			}
		}
		for (Entity passanger : entityIn.getPassengers()) {
			if (passanger.equals(orgEntity)) {
				return true;
			}
		}
		return false;
	}

	public void setAirborneState(boolean airborne) {
		this.original.getEntityData().set(AIRBORNE, airborne);
	}

	public boolean isAirborneState() {
		return this.original.getEntityData().get(AIRBORNE);
	}

	public void setLastAttackSuccess(boolean setter) {
		this.isLastAttackSuccess = setter;
	}

	public boolean isLastAttackSuccess() {
		return this.isLastAttackSuccess;
	}

	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		return !this.isLogicalClient();
	}

	public boolean isFirstPerson() {
		return false;
	}

	@Override
	public boolean overrideRender() {
		return true;
	}

	public boolean shouldBlockMoving() {
		return false;
	}

	public float getYRotLimit() {
		return 20.0F;
	}


	public double getXOld() {
		return this.original.xOld;
	}

	public double getYOld() {
		return this.original.yOld;
	}

	public double getZOld() {
		return this.original.zOld;
	}

	public float getYRot() {
		return this.original.yRot;
	}

	public void setYRot(float yRot) {
		this.original.yRot = yRot;
	}

	@Override
	public EntityState getEntityState() {
		return this.state;
	}

	public Hand getAttackingHand() {
		Pair<AnimationPlayer, AttackAnimation> layerInfo = this.getAnimator().findFor(AttackAnimation.class);

		if (layerInfo != null) {
			return layerInfo.getSecond().getPhaseByTime(layerInfo.getFirst().getElapsedTime()).hand;
		}
		return null;
	}

	public LivingMotion getCurrentLivingMotion() {
		return this.currentLivingMotion;
	}

	public List<LivingEntity> getCurrenltyAttackedEntities() {
		return this.getAnimator().getAnimationVariables(AttackAnimation.HIT_ENTITIES);
	}

	public List<LivingEntity> getCurrenltyHurtEntities() {
		return this.getAnimator().getAnimationVariables(AttackAnimation.HURT_ENTITIES);
	}

	public void removeHurtEntities() {
		this.getAnimator().getAnimationVariables(AttackAnimation.HIT_ENTITIES).clear();
		this.getAnimator().getAnimationVariables(AttackAnimation.HURT_ENTITIES).clear();
	}

	//TODO Implement
//	@OnlyIn(Dist.CLIENT)
//	public boolean flashTargetIndicator(LocalPlayerPatch playerpatch) {
//		TargetIndicatorCheckEvent event = new TargetIndicatorCheckEvent(playerpatch, this);
//		playerpatch.getEventListener().triggerEvents(EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, event);
//
//		return event.isCanceled();
//	}
}