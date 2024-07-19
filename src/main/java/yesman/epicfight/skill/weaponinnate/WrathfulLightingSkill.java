package yesman.epicfight.skill.weaponinnate;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

public class WrathfulLightingSkill extends SimpleWeaponInnateSkill {
    public WrathfulLightingSkill(Builder builder) {
        super(builder);
    }

    @Override
    public List<ITextComponent> getTooltipOnItem(ItemStack itemStack, CapabilityItem itemCap, PlayerPatch<?> playerpatch) {
//        List<Component> list = super.getTooltipOnItem(itemStack, itemCap, playerpatch);
//        this.generateTooltipforPhase(list, itemStack, itemCap, playerpatch, this.properties.get(1), "Thunder:");
//
//        return list;

        return null;
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        AttackAnimation anim = this.attackAnimation.get();

        for (Phase phase : anim.phases) {
            phase.addProperties(this.properties.get(0).entrySet());
            phase.addProperties(this.properties.get(1).entrySet());
        }

        return this;
    }
}