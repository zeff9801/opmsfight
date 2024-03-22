package yesman.epicfight.api.animation.types.procedural;

import net.minecraft.resources.IResourceManager;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EnderDragonDeathAnimation extends LongHitAnimation {
	public EnderDragonDeathAnimation(float convertTime, String path, Model model) {
		super(convertTime, path, model);
	}
	
	@Override
	public void loadAnimation(IResourceManager resourceManager) {
		loadBothSide(resourceManager, this);
		this.onLoaded();
	}

	@Override
	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {

	}
}