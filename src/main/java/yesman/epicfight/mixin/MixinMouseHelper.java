package yesman.epicfight.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = MouseHelper.class)
public class MixinMouseHelper {

	@Shadow private Minecraft minecraft;
	@Final @Shadow private MouseSmoother smoothTurnX;
	@Final @Shadow private MouseSmoother smoothTurnY;
	@Shadow private double accumulatedDX;
	@Shadow private double accumulatedDY;
	@Shadow private double lastMouseEventTime;
	
	@Inject(at = @At(value = "HEAD"), method = "turnPlayer()V", cancellable = true)
	private void epicfight_turnPlayer(CallbackInfo info) {
		//TODO I don't this is needed anymore
//		double d0 = NativeUtil.getTime();
//		double d1 = d0 - this.lastMouseEventTime;
//		this.lastMouseEventTime = d0;
//
//		MouseHelper self = (MouseHelper)((Object)this);
//
//		if (self.isMouseGrabbed() && this.minecraft.isWindowActive()) {
//			double d4 = this.minecraft.options.sensitivity * (double) 0.6F + (double) 0.2F;
//			double d5 = d4 * d4 * d4;
//			double d6 = d5 * 8.0D;
//			double d2;
//			double d3;
//
//			if (this.minecraft.options.smoothCamera) {
//				double d7 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d1 * d6);
//				double d8 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d1 * d6);
//				d2 = d7;
//				d3 = d8;
//			} else {
//				this.smoothTurnX.reset();
//				this.smoothTurnY.reset();
//				d2 = this.accumulatedDX * d6;
//				d3 = this.accumulatedDY * d6;
//			}
//
//			this.accumulatedDX = 0.0D;
//			this.accumulatedDY = 0.0D;
//			int i = 1;
//
//			if (this.minecraft.options.invertYMouse) {
//				i = -1;
//			}
//
//			this.minecraft.getTutorial().onMouse(d2, d3);
//
//			if (this.minecraft.player != null) {
//				LocalPlayerPatch playerpatch = (LocalPlayerPatch)this.minecraft.player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
//				RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
//
//				if (!playerpatch.getEntityState().turningLocked() || this.minecraft.player.isRidingJumpable()) {
//
//					if (renderEngine.isPlayerRotationLocked()) {
//						renderEngine.unlockRotation(this.minecraft.player);
//					}
//
//					this.minecraft.player.turn(d2, d3 * (double)i);
//				} else {
//					renderEngine.setCameraRotation((float)(d3 * i), (float)d2);
//				}
//			}
//		} else {
//			this.accumulatedDX = 0.0D;
//			this.accumulatedDY = 0.0D;
//		}
	}
}