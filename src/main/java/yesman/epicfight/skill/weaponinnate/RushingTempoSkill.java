package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.Map;

public class RushingTempoSkill extends WeaponInnateSkill {
    private final Map<StaticAnimation, AttackAnimation> comboAnimation = Maps.newHashMap();

    public RushingTempoSkill(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
    }

    @Override
    public void onRemoved(SkillContainer container) {
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
        DynamicAnimation animation = executer.getAnimator().getPlayerFor(null).getAnimation();

        if (this.comboAnimation.containsKey(animation)) {
            executer.playAnimationSynchronized(this.comboAnimation.get(animation), 0.0F);
            super.executeOnServer(executer, args);
        }
    }

    @Override
    public boolean checkExecuteCondition(PlayerPatch<?> executer) {
        EntityState playerState = executer.getEntityState();

        return this.comboAnimation.containsKey(executer.getAnimator().getPlayerFor(null).getAnimation()) && playerState.canUseSkill() && playerState.inaction();
    }

    @Override
    public List<ITextComponent> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
//        List<Component> list = Lists.newArrayList();
//        String traslatableText = this.getTranslationKey();
//
//        list.add(Component.translatable(traslatableText).withStyle(ChatFormatting.WHITE).append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
//        list.add(Component.translatable(traslatableText + ".tooltip", this.maxStackSize).withStyle(ChatFormatting.DARK_GRAY));
//
//        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
//        return list;

        return null;
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        this.comboAnimation.clear();
        this.comboAnimation.put(Animations.TACHI_AUTO1, (AttackAnimation)Animations.RUSHING_TEMPO1);
        this.comboAnimation.put(Animations.TACHI_AUTO2, (AttackAnimation)Animations.RUSHING_TEMPO2);
        this.comboAnimation.put(Animations.TACHI_AUTO3, (AttackAnimation)Animations.RUSHING_TEMPO3);

        this.comboAnimation.values().forEach((animation) -> {
            animation.phases[0].addProperties(this.properties.get(0).entrySet());
        });

        return this;
    }
}