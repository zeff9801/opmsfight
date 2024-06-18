package yesman.epicfight.api.data.reloader;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.TagBasedSeparativeCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;
import yesman.epicfight.world.capabilities.provider.ProviderItem;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class ItemCapabilityReloadListener extends JsonReloadListener {
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Map<Item, CompoundNBT> CAPABILITY_ARMOR_DATA_MAP = Maps.newHashMap();
	private static final Map<Item, CompoundNBT> CAPABILITY_WEAPON_DATA_MAP = Maps.newHashMap();
	
	public ItemCapabilityReloadListener() {
		super(GSON, "capabilities");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
			ResourceLocation rl = entry.getKey();
			String path = rl.getPath();
			
			if (path.contains("/")) {
				String[] str = path.split("/", 2);
				ResourceLocation registryName = new ResourceLocation(rl.getNamespace(), str[1]);
				Item item = ForgeRegistries.ITEMS.getValue(registryName);
				
				if (item == null) {
                    EpicFightMod.LOGGER.warn("Tried to add a capabiltiy for item {}, but it's not exist!", registryName);
					return;
				}
				
				CompoundNBT nbt = null;
				
				try {
					nbt = JsonToNBT.parseTag(entry.getValue().toString());
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
				
				if (str[0].equals("armors")) {
					CapabilityItem capability = deserializeArmor(item, nbt);
					ProviderItem.put(item, capability);
					CAPABILITY_ARMOR_DATA_MAP.put(item, nbt);
				} else if (str[0].equals("weapons")) {
					CapabilityItem capability = deserializeWeapon(item, nbt, null);
					ProviderItem.put(item, capability);
					CAPABILITY_WEAPON_DATA_MAP.put(item, nbt);
				}
			}
		}
		
		ProviderItem.addDefaultItems();
	}
	
	public static CapabilityItem deserializeArmor(Item item, CompoundNBT tag) {
		ArmorCapability.Builder builder = ArmorCapability.builder();
		
		if (tag.contains("attributes")) {
			CompoundNBT attributes = tag.getCompound("attributes");
			builder.weight(attributes.getDouble("weight")).stunArmor(attributes.getDouble("stun_armor"));
		}
		
		builder.item(item);
		
		return builder.build();
	}
	
	public static CapabilityItem deserializeWeapon(Item item, CompoundNBT tag, CapabilityItem.Builder defaultCapability) {
		CapabilityItem capability;
		
		if (tag.contains("variations")) {
			ListNBT jsonArray = tag.getList("variations", 10);
			List<Pair<Predicate<ItemStack>, CapabilityItem>> list = Lists.newArrayList();
			CapabilityItem.Builder innerDefaultCapabilityBuilder = tag.contains("type") ? WeaponCapabilityPresets.get(tag.getString("type")).apply(item) : CapabilityItem.builder();
			
			for (INBT jsonElement : jsonArray) {
				CompoundNBT innerTag = ((CompoundNBT)jsonElement);
				String nbtKey = innerTag.getString("nbt_key");
				String nbtValue = innerTag.getString("nbt_value");
				Predicate<ItemStack> predicate = (itemstack) -> {
					CompoundNBT compound = itemstack.getTag();
					
					if (compound == null) {
						return false;
					}
					
					return compound.contains(nbtKey) && compound.getString(nbtKey).equals(nbtValue);
				};
				
				list.add(Pair.of(predicate, deserializeWeapon(item, innerTag, innerDefaultCapabilityBuilder)));
			}
			
			if (tag.contains("attributes")) {
				CompoundNBT attributes = tag.getCompound("attributes");
				
				for (String key : attributes.getAllKeys()) {
					Map<Attribute, AttributeModifier> attributeEntry = deserializeAttributes(attributes.getCompound(key));
					
					for (Map.Entry<Attribute, AttributeModifier> attribute : attributeEntry.entrySet()) {
						innerDefaultCapabilityBuilder.addStyleAttibutes(Style.ENUM_MANAGER.get(key), Pair.of(attribute.getKey(), attribute.getValue()));
					}
				}
			}
			
			capability = new TagBasedSeparativeCapability(list, innerDefaultCapabilityBuilder.build());
		} else {
			CapabilityItem.Builder builder = tag.contains("type") ? WeaponCapabilityPresets.get(tag.getString("type")).apply(item) : CapabilityItem.builder();
			
			if (tag.contains("attributes")) {
				CompoundNBT attributes = tag.getCompound("attributes");
				
				for (String key : attributes.getAllKeys()) {
					Map<Attribute, AttributeModifier> attributeEntry = deserializeAttributes(attributes.getCompound(key));
					
					for (Map.Entry<Attribute, AttributeModifier> attribute : attributeEntry.entrySet()) {
						builder.addStyleAttibutes(Style.ENUM_MANAGER.get(key), Pair.of(attribute.getKey(), attribute.getValue()));
					}
				}
			}
			
			if (tag.contains("collider") && builder instanceof WeaponCapability.Builder) {
				CompoundNBT colliderTag = tag.getCompound("collider");
				Collider collider = deserializeCollider(item, colliderTag);
				((WeaponCapability.Builder)builder).collider(collider);
			}
			
			capability = builder.build();
		}
		
		return capability;
	}
	
	private static Map<Attribute, AttributeModifier> deserializeAttributes(CompoundNBT tag) {
		Map<Attribute, AttributeModifier> modifierMap = Maps.newHashMap();
		
		if (tag.contains("armor_negation")) {
			modifierMap.put(EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(tag.getDouble("armor_negation")));
		}
		if (tag.contains("impact")) {
			modifierMap.put(EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(tag.getDouble("impact")));
		}
		if (tag.contains("max_strikes")) {
			modifierMap.put(EpicFightAttributes.MAX_STRIKES.get(), EpicFightAttributes.getMaxStrikesModifier(tag.getInt("max_strikes")));
		}
		if (tag.contains("damage_bonus")) {
			modifierMap.put(Attributes.ATTACK_DAMAGE, EpicFightAttributes.getDamageBonusModifier(tag.getDouble("damage_bonus")));
		}
		if (tag.contains("speed_bonus")) {
			modifierMap.put(Attributes.ATTACK_SPEED, EpicFightAttributes.getSpeedBonusModifier(tag.getDouble("speed_bonus")));
		}
		
		return modifierMap;
	}
	
	private static Collider deserializeCollider(Item item, CompoundNBT tag) {
		int number = tag.getInt("number");
		
		if (number < 1) {
            EpicFightMod.LOGGER.warn("Datapack deserialization error: the number of colliders must bigger than 0! {}", item);
			return null;
		}
		
		ListNBT sizeVector = tag.getList("size", 6);
		ListNBT centerVector = tag.getList("center", 6);
		
		double sizeX = sizeVector.getDouble(0);
		double sizeY = sizeVector.getDouble(1);
		double sizeZ = sizeVector.getDouble(2);
		
		double centerX = centerVector.getDouble(0);
		double centerY = centerVector.getDouble(1);
		double centerZ = centerVector.getDouble(2);
		
		if (sizeX < 0 || sizeY < 0 || sizeZ < 0) {
            EpicFightMod.LOGGER.warn("Datapack deserialization error: the size of the collider must be non-negative! {}", item);
			return null;
		}
		
		if (number == 1) {
			return new OBBCollider(sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
		} else {
			return new MultiOBBCollider(number, sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
		}
	}
	
	public static Stream<CompoundNBT> getArmorDataStream() {
		Stream<CompoundNBT> tagStream = CAPABILITY_ARMOR_DATA_MAP.entrySet().stream().map((entry) -> {
			entry.getValue().putInt("id", Item.getId(entry.getKey()));
			return entry.getValue();
		});
		return tagStream;
	}
	
	public static Stream<CompoundNBT> getWeaponDataStream() {
		Stream<CompoundNBT> tagStream = CAPABILITY_WEAPON_DATA_MAP.entrySet().stream().map((entry) -> {
			entry.getValue().putInt("id", Item.getId(entry.getKey()));
			return entry.getValue();
		});
		return tagStream;
	}
	
	public static int armorCount() {
		return CAPABILITY_ARMOR_DATA_MAP.size();
	}
	
	public static int weaponCount() {
		return CAPABILITY_WEAPON_DATA_MAP.size();
	}
	
	private static boolean armorReceived = false;
	private static boolean weaponReceived = false;
	
	@OnlyIn(Dist.CLIENT)
	public static void reset() {
		armorReceived = false;
		weaponReceived = false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSync packet) {
		switch (packet.getType()) {
		case ARMOR:
			for (CompoundNBT tag : packet.getTags()) {
				Item item = Item.byId(tag.getInt("id"));
				CAPABILITY_ARMOR_DATA_MAP.put(item, tag);
			}
			armorReceived = true;
			break;
		case WEAPON:
			for (CompoundNBT tag : packet.getTags()) {
				Item item = Item.byId(tag.getInt("id"));
				CAPABILITY_WEAPON_DATA_MAP.put(item, tag);
			}
			weaponReceived = true;
			break;
		case MOB:
			break;
		}
		
		if (armorReceived && weaponReceived) {
			CAPABILITY_ARMOR_DATA_MAP.forEach((item, tag) -> {
				ProviderItem.put(item, deserializeArmor(item, tag));
			});
			
			CAPABILITY_WEAPON_DATA_MAP.forEach((item, tag) -> {
				ProviderItem.put(item, deserializeWeapon(item, tag, null));
			});
			
			ProviderItem.addDefaultItems();
		}
	}
	
	
}