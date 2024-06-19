package yesman.epicfight.world.effect;

import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;


public class VisibleMobEffect extends Effect {
	protected final Int2ObjectMap<ResourceLocation> icons;
	protected final Function<EffectInstance, Integer> metadataGetter;

	public VisibleMobEffect(EffectType category, int color, ResourceLocation textureLocation) {
		this(category, color, (effectInstance) -> 0, textureLocation);
	}

	public VisibleMobEffect(EffectType category, int color, Function<EffectInstance, Integer> metadataGetter, ResourceLocation... textureLocations) {
		super(EffectType.BENEFICIAL, color);
		this.icons = new Int2ObjectOpenHashMap<>();
		this.metadataGetter = metadataGetter;

		for (int i = 0; i < textureLocations.length; i++) {
			this.icons.put(i, textureLocations[i]);
		}
	}

	@SuppressWarnings("deprecation")
	public ResourceLocation getIcon(EffectInstance effectInstance) {
		return this.icons.get(this.metadataGetter.apply(effectInstance));
	}
}