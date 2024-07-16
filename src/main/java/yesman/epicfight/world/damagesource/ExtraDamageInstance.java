package yesman.epicfight.world.damagesource;


import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ExtraDamageInstance {
    public static final ExtraDamage TARGET_LOST_HEALTH = new ExtraDamage((attacker, itemstack, target, baseDamage, params) -> (target.getMaxHealth() - target.getHealth()) * params[0]);



    public static final ExtraDamage SWEEPING_EDGE_ENCHANTMENT = new ExtraDamage((attacker, itemstack, target, baseDamage, params) -> {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, itemstack);
        float modifier = (i > 0) ? (float)i / (i + 1.0F) : 0.0F;

        return baseDamage * modifier;
    });

    private final ExtraDamage calculator;
    private final float[] params;

    public ExtraDamageInstance(ExtraDamage calculator, float... params) {
        this.calculator = calculator;
        this.params = params;
    }

    public float[] getParams() {
        return this.params;
    }

    public float get(LivingEntity attacker, ItemStack hurtItem, LivingEntity target, float baseDamage) {
        return this.calculator.extraDamage.getBonusDamage(attacker, hurtItem, target, baseDamage, this.params);
    }

    @FunctionalInterface
    public interface ExtraDamageFunction {
        float getBonusDamage(LivingEntity attacker, ItemStack hurtItem, LivingEntity target, float baseDamage, float[] params);
    }

    public static class ExtraDamage {
        ExtraDamageFunction extraDamage;

        public ExtraDamage(ExtraDamageFunction extraDamage) {
            this.extraDamage = extraDamage;
        }

        public ExtraDamageInstance create(float... params) {
            return new ExtraDamageInstance(this, params);
        }
    }
}