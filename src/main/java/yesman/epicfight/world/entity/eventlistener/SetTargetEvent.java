package yesman.epicfight.world.entity.eventlistener;

import net.minecraft.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SetTargetEvent extends PlayerEvent<ServerPlayerPatch> {
    private final LivingEntity target;

    public SetTargetEvent(ServerPlayerPatch playerpatch, LivingEntity target) {
        super(playerpatch, false);

        this.target = target;
    }

    public LivingEntity getTarget() {
        return this.target;
    }
}