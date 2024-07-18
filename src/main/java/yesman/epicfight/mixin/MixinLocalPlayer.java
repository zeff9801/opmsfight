package yesman.epicfight.mixin;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CInputPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class)
public abstract class MixinLocalPlayer {

	@Unique
	private final ClientPlayerEntity epicfight$entity = (ClientPlayerEntity)(Object)this;
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;sendPosition()V", shift = At.Shift.BEFORE), method = "tick()V")
	private void epicfight_tick(CallbackInfo ci) {
		epicfight$entity.connection.send(new CInputPacket(epicfight$entity.xxa, epicfight$entity.zza, epicfight$entity.input.jumping, epicfight$entity.input.shiftKeyDown));
	}
}