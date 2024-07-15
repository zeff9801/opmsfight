package yesman.epicfight.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.UUID;

public class LethalSlicingSkill extends SpecialAttackSkill {
	private static final UUID EVENT_UUID = UUID.fromString("bfa79c04-97a5-11eb-a8b3-0242ac130003");
	private AttackAnimation elbow;
	private AttackAnimation swing;
	private AttackAnimation doubleSwing;
	
	public LethalSlicingSkill(Builder<? extends Skill> builder) {
		super(builder);
		this.elbow = (AttackAnimation)Animations.LETHAL_SLICING;
		this.swing = (AttackAnimation)Animations.LETHAL_SLICING_ONCE;
		this.doubleSwing = (AttackAnimation)Animations.LETHAL_SLICING_TWICE;
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		container.getExecuter().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {
			if (Animations.LETHAL_SLICING.equals(event.getAnimation())) {
				List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrenltyHurtEntities();
				if (hurtEntities.size() <= 1) {
					event.getPlayerPatch().reserveAnimation(this.swing);
				} else if (hurtEntities.size() > 1) {
					event.getPlayerPatch().reserveAnimation(this.doubleSwing);
				}
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecuter().getEventListener().removeListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
		executer.playAnimationSynchronized(this.elbow, 0.0F);
		super.executeOnServer(executer, args);
	}
	
	@Override
	public List<ITextComponent> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<ITextComponent> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Elbow:");
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Each Strike:");
		return list;
	}
	
	@Override
	public SpecialAttackSkill registerPropertiesToAnimation() {
		this.elbow.phases[0].addProperties(this.properties.get(0).entrySet());
		this.swing.phases[0].addProperties(this.properties.get(1).entrySet());
		this.doubleSwing.phases[0].addProperties(this.properties.get(1).entrySet());
		this.doubleSwing.phases[1].addProperties(this.properties.get(1).entrySet());
		return this;
	}
}