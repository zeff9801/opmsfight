package yesman.epicfight.skill.weaponinnate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.PacketBuffer;
import yesman.epicfight.api.animation.AttackAnimationProvider;
import yesman.epicfight.api.animation.StaticAnimationProvider;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.ChargeableSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

public class SteelWhirlwindSkill extends WeaponInnateSkill implements ChargeableSkill {
    private static final UUID EVENT_UUID = UUID.fromString("d2d057cc-f30f-11ed-a05b-0242ac120003");

    public static int getChargingPower(SkillContainer skillContainer) {
        return skillContainer.getDataManager().getDataValue(SkillDataKeys.CHARGING_POWER.get());
    }

    private StaticAnimationProvider chargingAnimation;
    private AttackAnimationProvider attackAnimation;

    public SteelWhirlwindSkill(Builder<? extends Skill> builder) {
        super(builder);

        this.chargingAnimation = () -> Animations.STEEL_WHIRLWIND_CHARGING;
        this.attackAnimation = () -> (AttackAnimation)Animations.STEEL_WHIRLWIND;
    }

    @Override
    public void onInitiate(SkillContainer container) {
        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            if (event.getPlayerPatch().isChargingSkill(this)) {
                ClientPlayerEntity clientPlayer = event.getPlayerPatch().getOriginal();
                clientPlayer.setSprinting(false);
                clientPlayer.sprintTriggerTime = -1;
                Minecraft mc = Minecraft.getInstance();
                ClientEngine.getInstance().controllEngine.setKeyBind(mc.options.keySprint, false);

                event.getMovementInput().forwardImpulse *= 1.0F - 0.8F * event.getPlayerPatch().getSkillChargingTicks() / 30.0F;
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);

        container.getExecuter().getEventListener().removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        AttackAnimation anim = this.attackAnimation.get();

        for (Phase phase : anim.phases) {
            phase.addProperties(this.properties.get(0).entrySet());
        }

        return this;
    }

    @Override
    public int getAllowedMaxChargingTicks() {
        return 60;
    }

    @Override
    public int getMaxChargingTicks() {
        return 30;
    }

    @Override
    public int getMinChargingTicks() {
        return 6;
    }

    @Override
    public void startCharging(PlayerPatch<?> caster) {
        caster.playAnimationSynchronized(this.chargingAnimation.get(), 0.0F);
    }

    @Override
    public void resetCharging(PlayerPatch<?> caster) {
    }

    @Override
    public void castSkill(ServerPlayerPatch caster, SkillContainer skillContainer, int chargingTicks, SPSkillExecutionFeedback feedbackPacket, boolean onMaxTick) {
        caster.getSkill(this).getDataManager().setDataSync(SkillDataKeys.CHARGING_POWER.get(), chargingTicks, caster.getOriginal());
        caster.playAnimationSynchronized(this.attackAnimation.get(), 0.0F);
        this.cancelOnServer(caster, null);
    }

    @Override
    public KeyBinding getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }

    @Override
    public void gatherChargingArguemtns(LocalPlayerPatch caster, ControllEngine controllEngine, PacketBuffer buffer) {
    }
}