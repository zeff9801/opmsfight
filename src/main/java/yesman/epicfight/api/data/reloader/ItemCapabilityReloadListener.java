package yesman.epicfight.api.data.reloader;

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
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.world.capabilities.item.*;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

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

			if (path.contains("/") && !path.contains("types")) {
				String[] str = path.split("/", 2);
				ResourceLocation registryName = new ResourceLocation(rl.getNamespace(), str[1]);

				if (!ForgeRegistries.ITEMS.containsKey(registryName)) {
					new NoSuchElementException("Item Capability Exception: No Item named " + registryName).printStackTrace();
					continue;
				}

				Item item = ForgeRegistries.ITEMS.getValue(registryName);
				CompoundNBT tag = null;

				try {
					tag = JsonToNBT.parseTag(entry.getValue().toString());
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}

				try {
					if (str[0].equals("armors")) {
						CapabilityItem capability = deserializeArmor(item, tag);
						ItemCapabilityProvider.put(item, capability);
						CAPABILITY_ARMOR_DATA_MAP.put(item, tag);
					} else if (str[0].equals("weapons")) {
						CapabilityItem capability = deserializeWeapon(item, tag);
						ItemCapabilityProvider.put(item, capability);
						CAPABILITY_WEAPON_DATA_MAP.put(item, tag);
					}
				} catch (Exception e) {
					EpicFightMod.LOGGER.warn("Error while deserializing datapack for " + registryName);
					e.printStackTrace();
				}
			}
		}

		ItemCapabilityProvider.addDefaultItems();
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

	public static CapabilityItem deserializeWeapon(Item item, CompoundNBT tag) {
		CapabilityItem capability;

		if (tag.contains("variations")) {
			ListNBT jsonArray = tag.getList("variations", 10);
			List<Pair<Condition<ItemStack>, CapabilityItem>> list = Lists.newArrayList();
			CapabilityItem.Builder innerDefaultCapabilityBuilder = tag.contains("type") ? WeaponTypeReloadListener.getOrThrow(tag.getString("type")).apply(item) : CapabilityItem.builder();

			if (tag.contains("attributes")) {
				CompoundNBT attributes = tag.getCompound("attributes");

				for (String key : attributes.getAllKeys()) {
					Map<Attribute, AttributeModifier> attributeEntry = deserializeAttributes(attributes.getCompound(key));

					for (Map.Entry<Attribute, AttributeModifier> attribute : attributeEntry.entrySet()) {
						innerDefaultCapabilityBuilder.addStyleAttibutes(Style.ENUM_MANAGER.getOrThrow(key), Pair.of(attribute.getKey(), attribute.getValue()));
					}
				}
			}

			for (INBT jsonElement : jsonArray) {
				CompoundNBT innerTag = ((CompoundNBT)jsonElement);
				//Supplier<Condition<ItemStack>> conditionProvider = EpicFightConditions.getConditionOrThrow(new ResourceLocation(innerTag.getString("condition")));
				//Condition<ItemStack> condition = conditionProvider.get().read(innerTag.getCompound("predicate"));

			//list.add(Pair.of(condition, deserializeWeapon(item, innerTag)));
			}

			capability = new TagBasedSeparativeCapability(list, innerDefaultCapabilityBuilder.build());
		} else {
			CapabilityItem.Builder builder = tag.contains("type") ? WeaponTypeReloadListener.getOrThrow(tag.getString("type")).apply(item) : CapabilityItem.builder();

			if (tag.contains("attributes")) {
				CompoundNBT attributes = tag.getCompound("attributes");

				for (String key : attributes.getAllKeys()) {
					Map<Attribute, AttributeModifier> attributeEntry = deserializeAttributes(attributes.getCompound(key));

					for (Map.Entry<Attribute, AttributeModifier> attribute : attributeEntry.entrySet()) {
						builder.addStyleAttibutes(Style.ENUM_MANAGER.getOrThrow(key), Pair.of(attribute.getKey(), attribute.getValue()));
					}
				}
			}

			if (tag.contains("collider") && builder instanceof WeaponCapability.Builder weaponCapBuilder) {
				CompoundNBT colliderTag = tag.getCompound("collider");

				try {
					Collider collider = ColliderPreset.deserializeSimpleCollider(colliderTag);
					weaponCapBuilder.collider(collider);
				} catch (IllegalArgumentException e) {
					EpicFightMod.LOGGER.warn("Cannot deserialize collider: " + e.getMessage());
					e.printStackTrace();
				}
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
	private static boolean weaponTypeReceived = false;

	public static void weaponTypeProcessedCheck() {
		weaponTypeReceived = true;
	}
	@OnlyIn(Dist.CLIENT)
	public static void reset() {
		armorReceived = false;
		weaponReceived = false;
		weaponTypeReceived = false;
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
			default:
				break;
		}
		if (weaponTypeReceived && armorReceived && weaponReceived) {
			CAPABILITY_ARMOR_DATA_MAP.forEach((item, tag) -> {
				try {
					CapabilityItem itemCap = deserializeWeapon(item, tag);
					ItemCapabilityProvider.put(item, itemCap);
				} catch (NoSuchElementException e) {
					e.printStackTrace();
					throw e;
				} catch (Exception e) {
					EpicFightMod.LOGGER.warn("Can't read item capability for " + item);
					e.printStackTrace();
				}
			});

			CAPABILITY_WEAPON_DATA_MAP.forEach((item, tag) -> {
				try {
					CapabilityItem itemCap = deserializeWeapon(item, tag);
					ItemCapabilityProvider.put(item, itemCap);
				} catch (NoSuchElementException e) {
					e.printStackTrace();
					throw e;
				} catch (Exception e) {
					EpicFightMod.LOGGER.warn("Can't read item capability for " + item);
					e.printStackTrace();
				}
			});

			ItemCapabilityProvider.addDefaultItems();
		}
	}
}