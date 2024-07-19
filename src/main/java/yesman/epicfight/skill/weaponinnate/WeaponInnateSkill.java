package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public abstract class WeaponInnateSkill extends Skill {
    public static Skill.Builder<WeaponInnateSkill> createWeaponInnateBuilder() {
        return (new Skill.Builder<WeaponInnateSkill>()).setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
    }

    protected List<Map<AttackPhaseProperty<?>, Object>> properties;

    public WeaponInnateSkill(Builder<? extends Skill> builder) {
        super(builder);

        this.properties = Lists.newArrayList();
    }

    @Override
    public boolean canExecute(PlayerPatch<?> executer) {
        if (executer.isLogicalClient()) {
            return super.canExecute(executer);
        } else {
            ItemStack itemstack = executer.getOriginal().getMainHandItem();

            return super.canExecute(executer) && EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(executer, itemstack) == this
                    && executer.getOriginal().getVehicle() == null && (!executer.getSkill(this).isActivated() || this.activateType == ActivateType.TOGGLE);
        }
    }

    @Override
    public List<ITextComponent> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<ITextComponent> list = Lists.newArrayList();
        String traslatableText = this.getTranslationKey();

        list.add(new TranslationTextComponent(TextFormatting.WHITE + traslatableText + TextFormatting.AQUA + "[%.0f]"));
        list.add(new TranslationTextComponent(TextFormatting.DARK_GRAY + traslatableText + ".tooltip"));

        return list;
    }

//    protected void generateTooltipforPhase(List<ITextComponent> list, ItemStack itemstack, CapabilityItem itemcap, PlayerPatch<?> playerpatch, Map<AttackPhaseProperty<?>, Object> propertyMap, String title) {
//        Multimap<Attribute, AttributeModifier> capAttributes = itemcap.getAttributeModifiers(EquipmentSlotType.MAINHAND, playerpatch);
//        double damage = playerpatch.getWeaponAttribute(Attributes.ATTACK_DAMAGE, itemstack);
//        double armorNegation = playerpatch.getWeaponAttribute(EpicFightAttributes.ARMOR_NEGATION.get(), itemstack);
//        double impact = playerpatch.getWeaponAttribute(EpicFightAttributes.IMPACT.get(), itemstack);
//        double maxStrikes = playerpatch.getWeaponAttribute(EpicFightAttributes.MAX_STRIKES.get(), itemstack);
//        ValueModifier damageModifier = ValueModifier.empty();
//        ValueModifier armorNegationModifier = ValueModifier.empty();
//        ValueModifier impactModifier = ValueModifier.empty();
//        ValueModifier maxStrikesModifier = ValueModifier.empty();
//
//        for (AttributeModifier modifier : capAttributes.get(EpicFightAttributes.ARMOR_NEGATION.get())) {
//            armorNegation += modifier.getAmount();
//        }
//
//        for (AttributeModifier modifier : capAttributes.get(EpicFightAttributes.IMPACT.get())) {
//            impact += modifier.getAmount();
//        }
//
//        for (AttributeModifier modifier : capAttributes.get(EpicFightAttributes.MAX_STRIKES.get())) {
//            maxStrikes += modifier.getAmount();
//        }
//
//        this.getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, propertyMap).ifPresent(damageModifier::merge);
//        this.getProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, propertyMap).ifPresent(armorNegationModifier::merge);
//        this.getProperty(AttackPhaseProperty.IMPACT_MODIFIER, propertyMap).ifPresent(impactModifier::merge);
//        this.getProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, propertyMap).ifPresent(maxStrikesModifier::merge);
//
//        impactModifier.merge(ValueModifier.multiplier(1.0F + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, itemstack) * 0.12F));
//
//        Double baseDamage = Double.valueOf(damage);
//        damage = damageModifier.getTotalValue(playerpatch.getModifiedBaseDamage((float)damage));
//        armorNegation = armorNegationModifier.getTotalValue((float)armorNegation);
//        impact = impactModifier.getTotalValue((float)impact);
//        maxStrikes = maxStrikesModifier.getTotalValue((float)maxStrikes);
//
//        list.add(new StringTextComponent(String.format("%s%stitle", TextFormatting.UNDERLINE, TextFormatting.GRAY)));
//
//        TranslationTextComponent damageComponent = new TranslationTextComponent(TextFormatting.DARK_GRAY + "damage_source.epicfight.damage", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage));
//
//        this.getProperty(AttackPhaseProperty.EXTRA_DAMAGE, propertyMap).ifPresent((extraDamageSet) -> {
//            extraDamageSet.forEach((extraDamage) -> {
//                extraDamage.setTooltips(itemstack, damageComponent, baseDamage);
//            });
//        });
//
//        list.add(damageComponent);
//
//        if (armorNegation != 0.0D) {
//            list.add(Component.translatable( EpicFightAttributes.ARMOR_NEGATION.get().getDescriptionId()
//                    , Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(armorNegation) + "%"
//                    ).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.DARK_GRAY));
//        }
//
//        if (impact != 0.0D) {
//            list.add(Component.translatable( EpicFightAttributes.IMPACT.get().getDescriptionId()
//                    , Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(impact)
//                    ).withStyle(ChatFormatting.AQUA)
//            ).withStyle(ChatFormatting.DARK_GRAY));
//        }
//
//        list.add(Component.translatable(EpicFightAttributes.MAX_STRIKES.get().getDescriptionId(),
//                Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxStrikes)).withStyle(ChatFormatting.WHITE)
//        ).withStyle(ChatFormatting.DARK_GRAY));
//
//        Optional<StunType> stunOption = this.getProperty(AttackPhaseProperty.STUN_TYPE, propertyMap);
//
//        stunOption.ifPresent((stunType) -> {
//            list.add(Component.translatable(stunType.toString()).withStyle(ChatFormatting.DARK_GRAY));
//        });
//
//        if (!stunOption.isPresent()) {
//            list.add(Component.translatable(StunType.SHORT.toString()).withStyle(ChatFormatting.DARK_GRAY));
//        }
//    }

    @SuppressWarnings("unchecked")
    protected <V> Optional<V> getProperty(AttackPhaseProperty<V> propertyKey, Map<AttackPhaseProperty<?>, Object> map) {
        return (Optional<V>) Optional.ofNullable(map.get(propertyKey));
    }

    public WeaponInnateSkill newProperty() {
        this.properties.add(Maps.newHashMap());

        return this;
    }

    public <T> WeaponInnateSkill addProperty(AttackPhaseProperty<T> propertyKey, T object) {
        this.properties.get(properties.size() - 1).put(propertyKey, object);

        return this;
    }
}