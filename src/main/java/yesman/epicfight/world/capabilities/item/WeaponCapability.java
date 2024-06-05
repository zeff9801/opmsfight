package yesman.epicfight.world.capabilities.item;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class WeaponCapability extends CapabilityItem {
	protected final Function<LivingEntityPatch<?>, Style> stylegetter;
	protected final Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
	protected final Skill passiveSkill;
	protected final SoundEvent smashingSound;
	protected final SoundEvent hitSound;
	protected final Collider weaponCollider;
	protected final Map<Style, List<StaticAnimation>> autoAttackMotions;
	protected final Map<Style, Skill> specialAttacks;
	protected final Map<Style, Map<LivingMotion, StaticAnimation>> livingMotionModifiers;
	protected final boolean canBePlacedOffhand;
	
	protected WeaponCapability(CapabilityItem.Builder builder) {
		super(builder);
		
		WeaponCapability.Builder weaponBuilder = (WeaponCapability.Builder)builder;
		
		this.autoAttackMotions = weaponBuilder.autoAttackMotionMap;
		this.specialAttacks = weaponBuilder.specialAttackMap;
		this.livingMotionModifiers = weaponBuilder.livingMotionModifiers;
		this.stylegetter = weaponBuilder.styleProvider;
		this.weaponCombinationPredicator = weaponBuilder.weaponCombinationPredicator;
		this.passiveSkill = weaponBuilder.passiveSkill;
		this.smashingSound = weaponBuilder.swingSound;
		this.hitSound = weaponBuilder.hitSound;
		this.weaponCollider = weaponBuilder.collider;
		this.canBePlacedOffhand = weaponBuilder.canBePlacedOffhand;
		this.attributeMap.putAll(weaponBuilder.attributeMap);
	}
	
	@Override
	public final List<StaticAnimation> getAutoAttckMotion(PlayerPatch<?> playerpatch) {
		return this.autoAttackMotions.get(this.getStyle(playerpatch));
	}
	@Override
	public boolean availableOnHorse() {
		return this.getMountAttackMotion() != null;
	}
	@Override
	public final Skill getSpecialAttack(PlayerPatch<?> playerpatch) {
		return this.specialAttacks.get(this.getStyle(playerpatch));
	}
	
	@Override
	public Skill getPassiveSkill() {
		return this.passiveSkill;
	}
	
	@Override
	public final List<StaticAnimation> getMountAttackMotion() {
		return this.autoAttackMotions.get(Styles.MOUNT);
	}
	
	@Override
	public Style getStyle(LivingEntityPatch<?> entitypatch) {
		return this.stylegetter.apply(entitypatch);
	}
	
	@Override
	public SoundEvent getSmashingSound() {
		return this.smashingSound;
	}
	
	@Override
	public SoundEvent getHitSound() {
		return this.hitSound;
	}
	
	@Override
	public HitParticleType getHitParticle() {
		return EpicFightParticles.HIT_BLADE.get();
	}
	
	@Override
	public Collider getWeaponCollider() {
		return this.weaponCollider != null ? this.weaponCollider : super.getWeaponCollider();
	}
	
	@Override
	public boolean canBePlacedOffhand() {
		return this.canBePlacedOffhand;
	}

	@Override
	public Map<LivingMotion, StaticAnimation> getLivingMotionModifier(LivingEntityPatch<?> player, Hand hand) {
		if (this.livingMotionModifiers == null || hand == Hand.OFF_HAND) {
			return super.getLivingMotionModifier(player, hand);
		}

		Map<LivingMotion, StaticAnimation> motions = this.livingMotionModifiers.getOrDefault(this.getStyle(player), Maps.newHashMap());
		this.livingMotionModifiers.getOrDefault(Styles.COMMON, Maps.newHashMap()).forEach(motions::putIfAbsent);

		return motions;
	}
	
	@Override
	public UseAction getUseAnimation(LivingEntityPatch<?> playerpatch) {
		if (this.livingMotionModifiers != null) {
			Style style = this.getStyle(playerpatch);

			if (this.livingMotionModifiers.containsKey(style)) {
				if (this.livingMotionModifiers.get(style).containsKey(LivingMotions.BLOCK)) {
					return UseAction.BLOCK;
				}
			}
		}

		return UseAction.NONE;
	}
	
	@Override
	public boolean canHoldInOffhandAlone() {
		return false;
	}
	
	@Override
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
		return super.checkOffhandValid(entitypatch) || this.weaponCombinationPredicator.apply(entitypatch);
	}
	
	public static WeaponCapability.Builder builder() {
		return new WeaponCapability.Builder();
	}
	
	public static class Builder extends CapabilityItem.Builder {
		Function<LivingEntityPatch<?>, Style> styleProvider;
		Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
		Skill passiveSkill;
		SoundEvent swingSound;
		SoundEvent hitSound;
		Collider collider;
		Map<Style, List<StaticAnimation>> autoAttackMotionMap;
		Map<Style, Skill> specialAttackMap;
		Map<Style, Map<LivingMotion, StaticAnimation>> livingMotionModifiers;
		boolean canBePlacedOffhand;
		
		protected Builder() {
			this.constructor = WeaponCapability::new;
			this.styleProvider = (entitypatch) -> Styles.ONE_HAND;
			this.weaponCombinationPredicator = (entitypatch) -> false;
			this.passiveSkill = null;
			this.swingSound = EpicFightSounds.WHOOSH;
			this.hitSound = EpicFightSounds.BLUNT_HIT;
			this.collider = ColliderPreset.FIST;
			this.autoAttackMotionMap = Maps.newHashMap();
			this.specialAttackMap = Maps.newHashMap();
			this.livingMotionModifiers = null;
			this.canBePlacedOffhand = true;
		}
		
		@Override
		public Builder category(WeaponCategory category) {
			super.category(category);
			return this;
		}
		
		public Builder styleProvider(Function<LivingEntityPatch<?>, Style> styleProvider) {
			this.styleProvider = styleProvider;
			return this;
		}
		
		public Builder passiveSkill(Skill passiveSkill) {
			this.passiveSkill = passiveSkill;
			return this;
		}
		
		public Builder swingSound(SoundEvent swingSound) {
			this.swingSound = swingSound;
			return this;
		}
		
		public Builder hitSound(SoundEvent hitSound) {
			this.hitSound = hitSound;
			return this;
		}
		
		public Builder collider(Collider collider) {
			this.collider = collider;
			return this;
		}
		
		public Builder canBePlacedOffhand(boolean canBePlacedOffhand) {
			this.canBePlacedOffhand = canBePlacedOffhand;
			return this;
		}
		
		public Builder livingMotionModifier(Style wieldStyle, LivingMotion livingMotion, StaticAnimation animation) {
			if (this.livingMotionModifiers == null) {
				this.livingMotionModifiers = Maps.<Style, Map<LivingMotion, StaticAnimation>>newHashMap();
			}
			
			if (!this.livingMotionModifiers.containsKey(wieldStyle)) {
				this.livingMotionModifiers.put(wieldStyle, Maps.<LivingMotion, StaticAnimation>newHashMap());
			}
			
			this.livingMotionModifiers.get(wieldStyle).put(livingMotion, animation);
			
			return this;
		}
		
		public Builder addStyleAttibutes(Style style, Pair<Attribute, AttributeModifier> attributePair) {
			super.addStyleAttibutes(style, attributePair);
			return this;
		}
		
		public Builder newStyleCombo(Style style, StaticAnimation... animation) {
			this.autoAttackMotionMap.put(style, Lists.newArrayList(animation));
			return this;
		}
		
		public Builder weaponCombinationPredicator(Function<LivingEntityPatch<?>, Boolean> predicator) {
			this.weaponCombinationPredicator = predicator;
			return this;
		}

		public Builder specialAttack(Style style, Skill specialAttack) {
			this.specialAttackMap.put(style, specialAttack);
			return this;
		}
	}
}