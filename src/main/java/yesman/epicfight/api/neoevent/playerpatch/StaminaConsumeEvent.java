package yesman.epicfight.api.neoevent.playerpatch;

import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEvent;

public class StaminaConsumeEvent extends PlayerEvent<PlayerPatch<?>> {
    private float amount;

    public StaminaConsumeEvent(PlayerPatch<?> playerpatch, float amount) {
        super(playerpatch, false);
        this.amount = amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return this.amount;
    }
}
