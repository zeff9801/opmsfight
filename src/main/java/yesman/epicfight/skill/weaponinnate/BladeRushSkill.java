
package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.StaticAnimationProvider;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.Map;
import java.util.UUID;

public class BladeRushSkill extends WeaponInnateSkill {
    public static Builder createBladeRushBuilder() {
        return new Builder().setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE)
                .putTryAnimation(EntityType.ZOMBIE, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.HUSK, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.DROWNED, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.SKELETON, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.STRAY, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.CREEPER, Animations.BLADE_RUSH_TRY);
    }

    private static final UUID EVENT_UUID = UUID.fromString("444a1a6a-c2f1-11eb-8529-0242ac130003");

    public static class Builder extends Skill.Builder<BladeRushSkill> {
        private final Map<EntityType<?>, StaticAnimation> tryAnimations = Maps.newHashMap();

        public Builder putTryAnimation(EntityType<?> entityType, StaticAnimation tryAnimation) {
            this.tryAnimations.put(entityType, tryAnimation);
            return this;
        }

        public Builder setCategory(SkillCategory category) {
            this.category = category;
            return this;
        }

        public Builder setActivateType(ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }
    }

    private final StaticAnimationProvider[] comboAnimations = new StaticAnimationProvider[3];
    private final Map<EntityType<?>, StaticAnimation> tryAnimations;

    public BladeRushSkill(Builder builder) {
        super(builder);

        this.comboAnimations[0] = () -> Animations.BLADE_RUSH_COMBO1;
        this.comboAnimations[1] = () -> Animations.BLADE_RUSH_COMBO2;
        this.comboAnimations[2] = () -> Animations.BLADE_RUSH_COMBO3;
        this.tryAnimations = builder.tryAnimations;
    }

    @Override
    public PacketBuffer gatherArguments(LocalPlayerPatch executer, ControllEngine controllEngine) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBoolean(true);

        return buf;
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecuter().getEventListener().addEventListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
            if (event.getDamageSource().getAnimation().idBetween(Animations.BLADE_RUSH_COMBO1, Animations.BLADE_RUSH_COMBO3) && this.tryAnimations.containsKey(event.getTarget().getType())) {
                EffectInstance effectInstance = event.getTarget().getEffect(EpicFightMobEffects.INSTABILITY.get());
                int amp = effectInstance == null ? 0 : effectInstance.getAmplifier() + 1;
                event.getTarget().addEffect(new EffectInstance(EpicFightMobEffects.INSTABILITY.get(), 100, amp));
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecuter().getEventListener().removeListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
        LivingEntity target = executer.getTarget();
        boolean execute = false;

        if (target != null) {
            if (target.hasEffect(EpicFightMobEffects.INSTABILITY.get()) && target.getEffect(EpicFightMobEffects.INSTABILITY.get()).getAmplifier() >= 2) {
                execute = true;
            }

            LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);

            if (entitypatch != null && entitypatch.getEntityState().hurtLevel() > 1 && this.tryAnimations.containsKey(target.getType())) {
                execute = true;
            }
        }

        if (execute) {
            executer.getSkill(this).getDataManager().setData(SkillDataKeys.COMBO_COUNTER.get(), 0);
            executer.playAnimationSynchronized(Animations.BLADE_RUSH_TRY, 0);
        } else {
            int animationId = executer.getSkill(this).getDataManager().getDataValue(SkillDataKeys.COMBO_COUNTER.get());
            executer.getSkill(this).getDataManager().setDataF(SkillDataKeys.COMBO_COUNTER.get(), (v) -> (v + 1) % this.comboAnimations.length);
            executer.playAnimationSynchronized(this.comboAnimations[animationId].get(), 0);
        }

        super.executeOnServer(executer, args);
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        ((AttackAnimation)Animations.BLADE_RUSH_COMBO1).phases[0].addProperties(this.properties.get(0).entrySet());
        ((AttackAnimation)Animations.BLADE_RUSH_COMBO2).phases[0].addProperties(this.properties.get(0).entrySet());
        ((AttackAnimation)Animations.BLADE_RUSH_COMBO3).phases[0].addProperties(this.properties.get(0).entrySet());
        ((AttackAnimation)Animations.BLADE_RUSH_EXECUTE_BIPED).phases[0].addProperties(this.properties.get(1).entrySet());
        return this;
    }

    @Override
    public boolean checkExecuteCondition(PlayerPatch<?> executer) {
        return executer.getTarget() != null && executer.getTarget().isAlive();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {

    }
}
