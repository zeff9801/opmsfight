package yesman.epicfight.mixin;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPUpdatePlayerInput;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = ClientPlayerEntity.class)
public abstract class MixinLocalPlayer {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;sendPosition()V", shift = At.Shift.BEFORE), method = "tick()V")
	private void epicfight_tick(CallbackInfo callbackInfo) {
		ClientPlayerEntity epicfight$entity = (ClientPlayerEntity) (Object) this;
		LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(epicfight$entity, LocalPlayerPatch.class);

		if (localPlayerPatch != null) {
			localPlayerPatch.dx = epicfight$entity.xxa;
			localPlayerPatch.dz = epicfight$entity.zza;
		}

		EpicFightNetworkManager.sendToServer(new CPUpdatePlayerInput(epicfight$entity.getId(), epicfight$entity.xxa, epicfight$entity.zza));
	}
}