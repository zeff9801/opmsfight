package yesman.epicfight.world.damagesource;

import java.util.Set;

import net.minecraft.item.ItemStack;
import yesman.epicfight.api.utils.math.ValueModifier;

public class DamageSourceElements {
    public ValueModifier damageModifier = ValueModifier.empty();
    public ItemStack hurtItem = ItemStack.EMPTY;
    public float impact = 0.5F;
    public float armorNegation = 0.0F;
    public StunType stunType = StunType.SHORT;
    public Set<ExtraDamageInstance> extraDamages;
}