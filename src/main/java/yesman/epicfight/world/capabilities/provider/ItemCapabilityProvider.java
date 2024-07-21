package yesman.epicfight.world.capabilities.provider;

import com.google.common.collect.Maps;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.*;

import java.util.Map;
import java.util.function.Function;

public class ItemCapabilityProvider implements ICapabilityProvider, NonNullSupplier<CapabilityItem> {
	private static final Map<Class<? extends Item>, Function<Item, CapabilityItem.Builder>> CAPABILITY_BY_CLASS = Maps.newHashMap();
	private static final Map<Item, CapabilityItem> CAPABILITIES = Maps.newHashMap();

	public static void registerWeaponTypesByClass() {
		CAPABILITY_BY_CLASS.put(ArmorItem.class, (item) -> ArmorCapability.builder().item(item));
		CAPABILITY_BY_CLASS.put(ShieldItem.class, WeaponCapabilityPresets.SHIELD);
		CAPABILITY_BY_CLASS.put(SwordItem.class, WeaponCapabilityPresets.SWORD);
		CAPABILITY_BY_CLASS.put(PickaxeItem.class, WeaponCapabilityPresets.PICKAXE);
		CAPABILITY_BY_CLASS.put(AxeItem.class, WeaponCapabilityPresets.AXE);
		CAPABILITY_BY_CLASS.put(ShovelItem.class, WeaponCapabilityPresets.SHOVEL);
		CAPABILITY_BY_CLASS.put(HoeItem.class, WeaponCapabilityPresets.HOE);
		CAPABILITY_BY_CLASS.put(BowItem.class, WeaponCapabilityPresets.BOW);
		CAPABILITY_BY_CLASS.put(CrossbowItem.class, WeaponCapabilityPresets.CROSSBOW);
		CAPABILITY_BY_CLASS.put(AbstractMapItem.class, (item) -> MapCapability.builder());
	}
	
	public static void put(Item item, CapabilityItem cap) {
		CAPABILITIES.put(item, cap);
	}

	public static CapabilityItem get(Item item) {
		return CAPABILITIES.getOrDefault(item, CAPABILITY_BY_CLASS.containsKey(item.getClass()) ? CAPABILITY_BY_CLASS.get(item.getClass()).apply(item).build() : null);
	}

	public static void clear() {
		CAPABILITIES.clear();
	}
	
	public static boolean has(Item item) {
		return CAPABILITIES.containsKey(item);
	}


	public static void addDefaultItems() {
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			if (!CAPABILITIES.containsKey(item)) {
				Class<?> clazz = item.getClass();
				CapabilityItem capability = null;

				for (; clazz != null && capability == null; clazz = clazz.getSuperclass()) {
					if (CAPABILITY_BY_CLASS.containsKey(clazz)) {
						capability = CAPABILITY_BY_CLASS.get(clazz).apply(item).build();
					}
				}

				if (capability != null) {
					CAPABILITIES.put(item, capability);
				}
			}
		}
	}
	
	private CapabilityItem capability;
	private LazyOptional<CapabilityItem> optional = LazyOptional.of(this);

	public ItemCapabilityProvider(ItemStack itemstack) {
		this.capability = CAPABILITIES.get(itemstack.getItem());

		if (this.capability instanceof TagBasedSeparativeCapability) {
			this.capability = this.capability.getResult(itemstack);
		}
	}
	
	public boolean hasCapability() {
		return this.capability != null;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == EpicFightCapabilities.CAPABILITY_ITEM ? this.optional.cast() : LazyOptional.empty();
	}
	
	@Override
	public CapabilityItem get() {
		return this.capability;
	}
}