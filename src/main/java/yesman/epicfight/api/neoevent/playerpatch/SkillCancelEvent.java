package yesman.epicfight.api.neoevent.playerpatch;

import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEvent;

public class SkillCancelEvent extends PlayerEvent<PlayerPatch<?>> {
    private final SkillContainer skillContainer;

    public SkillCancelEvent(PlayerPatch<?> playerpatch, SkillContainer skillContainer) {
        super(playerpatch, false);

        this.skillContainer = skillContainer;
    }

    public SkillContainer getSkillContainer() {
        return this.skillContainer;
    }
}
