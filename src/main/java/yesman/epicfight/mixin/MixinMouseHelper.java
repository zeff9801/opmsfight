package yesman.epicfight.mixin;

import net.minecraft.client.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = MouseHelper.class)
public abstract class MixinMouseHelper {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;turn(DD)V", shift = At.Shift.BEFORE), method = "turnPlayer()V", cancellable = true)
	private void epicfight_turnPlayer(CallbackInfo callbackInfo) {
		LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
		if (localPlayerPatch != null && localPlayerPatch.isTargetLockedOn()) {
			callbackInfo.cancel();
		}
	}
}