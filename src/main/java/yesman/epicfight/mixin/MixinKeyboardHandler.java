package yesman.epicfight.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyboardListener;
import net.minecraft.util.Util;
import yesman.epicfight.client.ClientEngine;


@Mixin(value = KeyboardListener.class)
public abstract class MixinKeyboardHandler {
	@Shadow
	private long debugCrashKeyTime = -1L;

	@Inject(at = @At(value = "HEAD"), method = "handleDebugKeys(I)Z", cancellable = true)
	private void epicfight_handleDebugKeys(int key, CallbackInfoReturnable<Boolean> info) {
		if (!(this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L)) {
            if (key == GLFW.GLFW_KEY_Y) {
                boolean flag = ClientEngine.getInstance().switchArmorModelDebuggingMode();
                this.debugFeedbackTranslated(flag ? "debug.armor_model_debugging.on" : "debug.armor_model_debugging.off");
                info.cancel();
                info.setReturnValue(true);
            }
		}
	}
	
	@Shadow
	private void debugFeedbackTranslated(String p_90914_, Object... p_90915_) {}
}