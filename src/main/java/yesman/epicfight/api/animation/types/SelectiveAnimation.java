package yesman.epicfight.api.animation.types;

import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.TypeFlexibleHashMap.TypeKey;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.function.Function;

public class SelectiveAnimation extends StaticAnimation {
	public static final TypeKey<Integer> PREVIOUS_STATE = new TypeKey<>() {
		public Integer defaultValue() {
			return -1;
		}
	};
	
	private final Function<LivingEntityPatch<?>, Integer> selector;
	private final StaticAnimation[] animations;
	
	/**
	 * All animations should have same priority and layer type
	 */
	public SelectiveAnimation(Function<LivingEntityPatch<?>, Integer> selector, String path, StaticAnimation... animations) {
		super(0.15F, false, path, null);
		
		this.selector = selector;
		this.animations = animations;
	}
	
	@Override
	public AnimationClip getAnimationClip() {
		return AnimationManager.getInstance().getStaticAnimationClip(this.animations[0]);
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		int result = this.selector.apply(entitypatch);
		
		entitypatch.getAnimator().playAnimation(this.animations[result], 0.0F);
		entitypatch.getAnimator().putAnimationVariable(PREVIOUS_STATE, result);
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
	}
	
	@Override
	public void end(LivingEntityPatch<?> entitypatch, DynamicAnimation nextAnimation, boolean isEnd) {
		super.end(entitypatch, nextAnimation, isEnd);
		entitypatch.getAnimator().removeAnimationVariables(PREVIOUS_STATE);
	}
	
	@Override
	public boolean isMetaAnimation() {
		return true;
	}
	
	@Override
	public void loadAnimation(IResourceManager resourceManager) {
		for (StaticAnimation anim : this.animations) {
			anim.addEvents(StaticAnimationProperty.EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
				int result = this.selector.apply(entitypatch);
				
				if (entitypatch.getAnimator().getAnimationVariables(PREVIOUS_STATE) != result) {
					entitypatch.getAnimator().playAnimation(this.animations[result], 0.0F);
					entitypatch.getAnimator().putAnimationVariable(PREVIOUS_STATE, result);
				}
				
			}, AnimationEvent.Side.BOTH));
		}
	}
	
	@Override
	public List<StaticAnimation> getClipHolders() {
		return List.of(this.animations);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public Layer.Priority getPriority() {
		return this.animations[0].getPriority();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public Layer.LayerType getLayerType() {
		return this.animations[0].getLayerType();
	}
}