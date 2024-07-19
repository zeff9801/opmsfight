package yesman.epicfight.skill.weaponinnate;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.UUID;

public class GuillotineAxeSkill extends SimpleWeaponInnateSkill {
    private static final UUID EVENT_UUID = UUID.fromString("b84e577a-c653-11ed-afa1-0242ac120002");

    public GuillotineAxeSkill(SimpleWeaponInnateSkill.Builder builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecuter().getEventListener().addEventListener(EventType.DEALT_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
            if (event.getDamageSource().getAnimation() == Animations.THE_GUILLOTINE) {
                ValueModifier damageModifier = ValueModifier.empty();
                this.getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, this.properties.get(0)).ifPresent(damageModifier::merge);
                damageModifier.merge(ValueModifier.multiplier(0.8F));
                float health = event.getTarget().getHealth();
                float executionHealth = damageModifier.getTotalValue((float)event.getPlayerPatch().getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE));

                if (health < executionHealth) {
                    if (event.getDamageSource() != null) {
                        event.getDamageSource().addDamageType(EpicFightDamageSources.TYPE.EXECUTION);
                    }
                }
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);

        container.getExecuter().getEventListener().removeListener(EventType.DEALT_DAMAGE_EVENT_HURT, EVENT_UUID);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<ITextComponent> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
//        List<Component> list = Lists.newArrayList();
//        List<Object> tooltipArgs = Lists.newArrayList();
//        String traslatableText = this.getTranslationKey();
//        Multimap<Attribute, AttributeModifier> attributes = itemstack.getAttributeModifiers(EquipmentSlot.MAINHAND);
//        double damage = playerpatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + EnchantmentHelper.getDamageBonus(itemstack, MobType.UNDEFINED);
//        ValueModifier damageModifier = ValueModifier.empty();
//
//        Set<AttributeModifier> damageModifiers = Sets.newHashSet();
//        damageModifiers.addAll(playerpatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).getModifiers());
//        damageModifiers.addAll(attributes.get(Attributes.ATTACK_DAMAGE));
//
//        for (AttributeModifier modifier : damageModifiers) {
//            damage += modifier.getAmount();
//        }
//
//        this.getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, this.properties.get(0)).ifPresent(damageModifier::merge);
//        damageModifier.merge(ValueModifier.multiplier(0.8F));
//        tooltipArgs.add(ChatFormatting.RED + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damageModifier.getTotalValue((float)damage)));
//
//        list.add(Component.translatable(traslatableText).withStyle(ChatFormatting.WHITE).append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
//        list.add(Component.translatable(traslatableText + ".tooltip", tooltipArgs.toArray(new Object[0])).withStyle(ChatFormatting.DARK_GRAY));
//
//        this.generateTooltipforPhase(list, itemstack, cap, playerpatch, this.properties.get(0), "Each Strike:");
//
//        return list;

        return null;
    }
}