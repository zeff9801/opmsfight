package yesman.epicfight.api.utils;

import net.minecraft.util.ObjectIntIdentityMap;

public class ClearableIdMapper<I> extends ObjectIntIdentityMap<I> {
	public ClearableIdMapper() {
		super(512);
	}

	public ClearableIdMapper(int size) {
		super(size);
	}

	public void clear() {
		this.tToId.clear();
		this.idToT.clear();
		this.nextId = 0;
	}

	public boolean contains(int animationId) {
		return animationId >= 0 && animationId < this.idToT.size() && this.idToT.get(animationId) != null;
	}
}