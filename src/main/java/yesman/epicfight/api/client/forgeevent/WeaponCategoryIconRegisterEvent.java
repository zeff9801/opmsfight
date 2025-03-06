package yesman.epicfight.api.client.forgeevent;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class WeaponCategoryIconRegisterEvent extends Event implements IModBusEvent {
	final Map<WeaponCategory, ItemStack> registry;
	
	public WeaponCategoryIconRegisterEvent(Map<WeaponCategory, ItemStack> registry) {
		this.registry = registry;
	}
	
	public void registerCategory(WeaponCategory weaponCategory, Item item) {
		this.registry.put(weaponCategory, new ItemStack(item));
	}
	
	public void registerCategory(WeaponCategory weaponCategory, ItemStack itemstack) {
		this.registry.put(weaponCategory, itemstack);
	}
}