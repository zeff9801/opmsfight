package yesman.epicfight.api.animation.types;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.ExtendedDamageSource;
import yesman.epicfight.api.utils.math.ExtraDamageType;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;

public class SpecialAttackAnimation extends AttackAnimation {
	public SpecialAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, String index, String path, Model model) {
		this(convertTime, path, model, new Phase(antic, preDelay, contact, recovery, index, collider));
	}
	
	public SpecialAttackAnimation(float convertTime, String path, Model model, Phase... phases) {
		super(convertTime, path, model, phases);
	}
	
	@Override
	protected float getDamageTo(LivingEntityPatch<?> entitypatch, LivingEntity target, Phase phase, ExtendedDamageSource source) {
		float f = entitypatch.getDamageTo(target, source, phase.hand);
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, entitypatch.getOriginal());
		ValueModifier cor = new ValueModifier(0, (i > 0) ? 1.0F + (float)i / (float)(i + 1.0F) : 1.0F, 0);
		phase.getProperty(AttackPhaseProperty.DAMAGE).ifPresent((opt) -> cor.merge(opt));
		float totalDamage = cor.getTotalValue(f);
		ExtraDamageType extraCalculator = phase.getProperty(AttackPhaseProperty.EXTRA_DAMAGE).orElse(null);
		
		if (extraCalculator != null) {
			totalDamage += extraCalculator.get(entitypatch.getOriginal(), target);
		}
		
		return totalDamage;
	}
}