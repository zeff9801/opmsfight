package yesman.epicfight.mixin;

import net.minecraft.entity.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;

@Mixin(value = MobEntity.class)
public abstract class MixinMob {
	@Inject(at = @At(value = "HEAD"), method = "serverAiStep()V", cancellable = true)
	private void epicfight_serverAiStep(CallbackInfo info) {
		MobEntity self = (MobEntity)((Object)this);
		HurtableEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, HurtableEntityPatch.class);
		
		if (entitypatch != null && entitypatch.isStunned()) {
			info.cancel();
		}
	}
}