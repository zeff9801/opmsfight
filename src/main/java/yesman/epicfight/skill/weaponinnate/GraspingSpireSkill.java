
package yesman.epicfight.skill.weaponinnate;

import net.minecraft.network.PacketBuffer;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

public class GraspingSpireSkill extends WeaponInnateSkill {
    private static final UUID EVENT_UUID = UUID.fromString("3fa26bbc-d14e-11ed-afa1-0242ac120002");

    private AnimationProvider<AttackAnimation> first;
    private AnimationProvider<AttackAnimation> second;

    public GraspingSpireSkill(Builder<? extends Skill> builder) {
        super(builder);
        this.first = () -> (AttackAnimation)Animations.GRASPING_SPIRAL_FIRST;
        this.second = () -> (AttackAnimation)Animations.GRASPING_SPIRAL_SECOND;
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecuter().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {
            if (this.first.get().equals(event.getAnimation())) {
                container.getDataManager().setDataSync(SkillDataKeys.LAST_HIT_COUNT.get(), event.getPlayerPatch().getCurrenltyHurtEntities().size(), event.getPlayerPatch().getOriginal());
            }
        });

        container.getExecuter().getEventListener().addEventListener(EventType.DEALT_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
            if (this.second.get().equals(event.getDamageSource().getAnimation())) {
                float impact = event.getDamageSource().getImpact();
                event.getDamageSource().setImpact(impact + container.getDataManager().getDataValue(SkillDataKeys.LAST_HIT_COUNT.get()) * 0.4F);
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        container.getExecuter().getEventListener().removeListener(EventType.DEALT_DAMAGE_EVENT_HURT, EVENT_UUID);
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
        executer.playAnimationSynchronized(this.first.get(), 0.0F);
        super.executeOnServer(executer, args);
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        this.first.get().phases[0].addProperties(this.properties.get(0).entrySet());
        this.second.get().phases[0].addProperties(this.properties.get(1).entrySet());

        return this;
    }
}
