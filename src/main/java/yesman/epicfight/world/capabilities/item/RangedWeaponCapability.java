
package yesman.epicfight.world.capabilities.item;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.Hand;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RangedWeaponCapability extends CapabilityItem {
	protected Map<LivingMotion, AnimationProvider<?>> rangeAnimationModifiers;
	protected ZoomInType zoomInType;

	protected RangedWeaponCapability(CapabilityItem.Builder builder) {
		super(builder);

		RangedWeaponCapability.Builder rangedBuilder = (RangedWeaponCapability.Builder)builder;
		this.rangeAnimationModifiers = rangedBuilder.rangeAnimationModifiers;
		this.zoomInType = rangedBuilder.zoomInType;
	}

	@Override
	public void setConfigFileAttribute(double armorNegation1, double impact1, int maxStrikes1, double armorNegation2, double impact2, int maxStrikes2) {
		this.addStyleAttributes(Styles.RANGED, armorNegation1, impact1, maxStrikes1);
	}

	@Override
	public Map<LivingMotion, AnimationProvider<?>> getLivingMotionModifier(LivingEntityPatch<?> playerdata, Hand hand) {
		if (hand == Hand.MAIN_HAND) {
			return this.rangeAnimationModifiers;
		}

		return super.getLivingMotionModifier(playerdata, hand);
	}

	@Override
	public boolean availableOnHorse() {
		return true;
	}

	@Override
	public boolean canBePlacedOffhand() {
		return false;
	}

	public static RangedWeaponCapability.Builder builder() {
		return new RangedWeaponCapability.Builder();
	}

	@Override
	public ZoomInType getZoomInType() {
		return this.zoomInType;
	}

	public static class Builder extends CapabilityItem.Builder {
		private Map<LivingMotion, AnimationProvider<?>> rangeAnimationModifiers;
		private ZoomInType zoomInType = ZoomInType.USE_TICK;

		protected Builder() {
			this.category = WeaponCategories.RANGED;
			this.constructor = RangedWeaponCapability::new;
			this.rangeAnimationModifiers = Maps.newHashMap();
		}

		public Builder addAnimationsModifier(LivingMotion livingMotion, AnimationProvider<?> animations) {
			this.rangeAnimationModifiers.put(livingMotion, animations);
			return this;
		}

		public Builder zoomInType(ZoomInType zoomInType) {
			this.zoomInType = zoomInType;
			return this;
		}
	}
}
