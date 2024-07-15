package yesman.epicfight.api.forgeevent;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Map;
import java.util.function.Function;

public class WeaponCapabilityPresetRegistryEvent extends Event implements IModBusEvent {
	private final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry;

	public WeaponCapabilityPresetRegistryEvent(Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry) {
		this.typeEntry = typeEntry;
	}

	public Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> getTypeEntry() {
		return this.typeEntry;
	}
}