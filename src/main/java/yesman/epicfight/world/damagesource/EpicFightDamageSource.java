package yesman.epicfight.world.damagesource;

import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.ExtendedDamageSource;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;

import java.util.Set;

public class EpicFightDamageSource extends EntityDamageSource {

	private DamageSourceElements damageSourceElements = new DamageSourceElements();
	private StaticAnimation animation;
	private Vector3d initialPosition;

	private Entity directEntity; //If projectile, this is the entity that cast it

	public EpicFightDamageSource(DamageSource from) {
		this(from.msgId, from.getEntity());
	}

	public EpicFightDamageSource(String sourceName, Entity sourceOwner) {
        super(sourceName, sourceOwner);
        this.initialPosition = sourceOwner.position();
	}

	public EpicFightDamageSource(String sourceName, Entity sourceOwner, Entity directSourceOwner) {
		super(sourceName, sourceOwner);
		this.initialPosition = sourceOwner.position();
		this.directEntity = directSourceOwner;
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

	public EpicFightDamageSource setStunType(ExtendedDamageSource.StunType stunType) {
		this.getDamageSourceElements().stunType = stunType;
		return this;
	}

	public ExtendedDamageSource.StunType getStunType() {
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
}