package yesman.epicfight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;

@Mixin(value = LightTexture.class)
public abstract class MixinLightTexture {
	@Inject(at = @At(value = "HEAD"), method = "updateLightTexture(F)V", cancellable = true)
	private void epicfight_head_updateLightTexture(CallbackInfo info) {
		OverlayManager overlayManager = ClientEngine.getInstance().renderEngine.getOverlayManager();
		
		if (overlayManager.isGammaChanged()) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.options.gamma = overlayManager.getModifiedGamma(minecraft.options.gamma);
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "updateLightTexture(F)V", cancellable = true)
	private void epicfight_tail_updateLightTexture(CallbackInfo info) {
		OverlayManager overlayManager = ClientEngine.getInstance().renderEngine.getOverlayManager();
		
		if (overlayManager.isGammaChanged()) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.options.gamma = overlayManager.getOriginalGamma();			
		}
	}
}