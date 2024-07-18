package yesman.epicfight.api.forgeevent;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

import java.util.Map;

public class AnimationRegistryEvent extends Event implements IModBusEvent {
	private Map<String, Runnable> registryMap;

	public AnimationRegistryEvent(Map<String, Runnable> registryMap) {
		this.registryMap = registryMap;
	}

	public Map<String, Runnable> getRegistryMap() {
		return this.registryMap;
	}
}