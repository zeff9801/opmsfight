package yesman.epicfight.mixin;

import net.minecraft.client.GameSettings;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft {
	@Shadow protected abstract void continueAttack(boolean p_147115_1_);

	@Shadow @Nullable public Screen screen;

	@Shadow @Final public GameSettings options;

	@Shadow @Final public MouseHelper mouseHandler;

	@Inject(at = @At(value = "HEAD"), method = "handleKeybinds()V", cancellable = true)
	private void epicfight_handleKeybinds(CallbackInfo info) {
		ClientEngine.getInstance().controllEngine.handleEpicFightKeyMappings();
	}

	/**
	 * @author 1ost_
	 * @reason Allow multiplayer in debugg mode
	 */
	@Overwrite
	public boolean allowsMultiplayer() {
		return true;
	}

	/**
	 * @author 1ost_
	 * @reason Disable block breaking while in combat mode. Disabling hotkeys doesn't work that well
	 */
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
	private void redirect_continueAttack(Minecraft instance, boolean p_147115_1_) {
		boolean inComatMode = false;

		LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(instance.player, LocalPlayerPatch.class);
		if (playerPatch != null) {
			inComatMode = playerPatch.isBattleMode();
		}


		this.continueAttack(this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed() && !inComatMode);
	}
}