package yesman.epicfight.client.gui.screen.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.ClientEngine;

@OnlyIn(Dist.CLIENT)
public class FlickeringOverlay extends OverlayManager.Overlay {
	private float time = (float)-Math.PI;
	private final float deltaTime;
	private final float strength;
	private final double initialGamma;

	public FlickeringOverlay(float deltaTime, float strength) {
		this.deltaTime = deltaTime;
		this.strength = strength;
		Minecraft minecraft = Minecraft.getInstance();
		this.initialGamma = minecraft.options.gamma;
	}

	@Override
	public boolean render(int xResolution, int yResolution) {
		this.time += this.deltaTime;
		float darkenAmount = MathHelper.clamp((float)Math.sin(this.time), -1.0F, 0.0F);

		OverlayManager overlayManager = ClientEngine.instance.renderEngine.getOverlayManager();
		overlayManager.setModifiedGamma(this.initialGamma + darkenAmount * strength);

        return this.time >= 0;
    }
}