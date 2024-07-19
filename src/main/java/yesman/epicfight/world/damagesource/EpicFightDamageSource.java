package yesman.epicfight.world.damagesource;

import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;

import java.util.List;
import java.util.Set;

	public class EpicFightDamageSource extends EntityDamageSource {

	private DamageSourceElements damageSourceElements = new DamageSourceElements();
	private StaticAnimation animation;
	private Vector3d initialPosition;

	private Entity directEntity; //If projectile, this is the entity that cast it

	private List<EpicFightDamageSources.TYPE> sourceTypes;

	public EpicFightDamageSource(DamageSource damageSource) {
		this(damageSource.msgId, damageSource.getEntity(), damageSource.getDirectEntity());
	}

	public EpicFightDamageSource(String sourceIdentifier, Entity sourceOwner, Entity directSourceOwner) {
		super(sourceIdentifier, sourceOwner);
		this.directEntity = directSourceOwner;
		this.sourceTypes.add(EpicFightDamageSources.TYPE.VANILLA_GENERIC);
		this.initialPosition = sourceOwner.position();
	}

	public EpicFightDamageSource(EpicFightDamageSources.TYPE sourceType, Entity sourceOwner) {
		super(sourceType.identifierName, sourceOwner);
		this.sourceTypes.add(sourceType);
		this.initialPosition = sourceOwner.position();
	}

	public EpicFightDamageSource(EpicFightDamageSources.TYPE sourceType, Entity sourceOwner, Entity directSourceOwner) {
		super(sourceType.identifierName, sourceOwner);
		this.sourceTypes.add(sourceType);
		this.initialPosition = sourceOwner.position();
		this.directEntity = directSourceOwner;
	}

	public boolean isIndirect() {
		return this.directEntity != null && this.directEntity != this.entity; //Damagesource has a direct entity that's not the same as the entity that caused the damage
	}


	public DamageSourceElements getDamageSourceElements() {
		return damageSourceElements;
	}

	public EpicFightDamageSource setHurtItem(ItemStack hurtItem) {
		this.getDamageSourceElements().hurtItem = hurtItem;
		return this;
	}

	public ItemStack getHurtItem() {
		return this.getDamageSourceElements().hurtItem;
	}

	public EpicFightDamageSource setDamageModifier(ValueModifier damageModifier) {
		this.getDamageSourceElements().damageModifier = damageModifier;
		return this;
	}

	public ValueModifier getDamageModifier() {
		return this.getDamageSourceElements().damageModifier;
	}

	public EpicFightDamageSource setImpact(float f) {
		this.getDamageSourceElements().impact = f;
		return this;
	}

	public float getImpact() {
		return this.getDamageSourceElements().impact;
	}

	public EpicFightDamageSource setArmorNegation(float f) {
		this.getDamageSourceElements().armorNegation = f;
		return this;
	}

	public float getArmorNegation() {
		return this.getDamageSourceElements().armorNegation;
	}

	public EpicFightDamageSource setStunType(StunType stunType) {
		this.getDamageSourceElements().stunType = stunType;
		return this;
	}

	public StunType getStunType() {
		return this.getDamageSourceElements().stunType;
	}

	public EpicFightDamageSource addExtraDamage(ExtraDamageInstance extraDamage) {
		if (this.getDamageSourceElements().extraDamages == null) {
			this.getDamageSourceElements().extraDamages = Sets.newHashSet();
		}

		this.getDamageSourceElements().extraDamages.add(extraDamage);

		return this;
	}

	public Set<ExtraDamageInstance> getExtraDamages() {
		return this.getDamageSourceElements().extraDamages;
	}


	public EpicFightDamageSource setInitialPosition(Vector3d initialPosition) {
		this.initialPosition = initialPosition;
		return this;
	}

	public Vector3d getInitialPosition() {
		return initialPosition;
	}

	public boolean isBasicAttack() {
		if (this.animation instanceof AttackAnimation) {
			return this.animation.isBasicAttackAnimation();
		}
		return false;
	}

	public EpicFightDamageSource setAnimation(StaticAnimation animation) {
		this.animation = animation;
		return this;
	}

	public StaticAnimation getAnimation() {
		return this.animation == null ? Animations.DUMMY_ANIMATION : this.animation;
	}

	public void addDamageType(EpicFightDamageSources.TYPE type) {
		this.sourceTypes.add(type);
	}

	public boolean is(EpicFightDamageSources.TYPE type) {
		return this.sourceTypes.contains(type);
	}
}