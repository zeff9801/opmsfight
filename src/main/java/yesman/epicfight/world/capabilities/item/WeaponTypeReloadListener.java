package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ibm.icu.impl.Pair;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class
WeaponTypeReloadListener extends JsonReloadListener {

	public static void registerDefaultWeaponTypes() {
		Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry = Maps.newHashMap();
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "axe"), WeaponCapabilityPresets.AXE);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "fist"), WeaponCapabilityPresets.FIST);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "hoe"), WeaponCapabilityPresets.HOE);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "pickaxe"), WeaponCapabilityPresets.PICKAXE);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "shovel"), WeaponCapabilityPresets.SHOVEL);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "sword"), WeaponCapabilityPresets.SWORD);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "spear"), WeaponCapabilityPresets.SPEAR);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "greatsword"), WeaponCapabilityPresets.GREATSWORD);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "katana"), WeaponCapabilityPresets.KATANA);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "tachi"), WeaponCapabilityPresets.TACHI);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "longsword"), WeaponCapabilityPresets.LONGSWORD);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "dagger"), WeaponCapabilityPresets.DAGGER);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "bow"), WeaponCapabilityPresets.BOW);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "crossbow"), WeaponCapabilityPresets.CROSSBOW);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "trident"), WeaponCapabilityPresets.TRIDENT);
		typeEntry.put(new ResourceLocation(EpicFightMod.MODID, "shield"), WeaponCapabilityPresets.SHIELD);

		WeaponCapabilityPresetRegistryEvent weaponCapabilityPresetRegistryEvent = new WeaponCapabilityPresetRegistryEvent(typeEntry);
		ModLoader.get().postEvent(weaponCapabilityPresetRegistryEvent);
		PRESETS.putAll(weaponCapabilityPresetRegistryEvent.getTypeEntry());
	}

	public static final String DIRECTORY = "capabilities/weapons/types";


	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> PRESETS = Maps.newHashMap();
	private static final Map<ResourceLocation, CompoundNBT> TAGMAP = Maps.newHashMap();

	public WeaponTypeReloadListener() {
		super(GSON, DIRECTORY);
	}


	@Override
	protected void apply(Map<ResourceLocation, JsonElement> packEntry, IResourceManager resourceManager, IProfiler profilerFiller) {
		clear();

		for (Map.Entry<ResourceLocation, JsonElement> entry : packEntry.entrySet()) {
			CompoundNBT nbt = null;

			try {
				nbt = JsonToNBT.parseTag(entry.getValue().toString());
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}

			try {
				WeaponCapability.Builder builder = deserializeWeaponCapabilityBuilder(nbt);

				PRESETS.put(entry.getKey(), (itemstack) -> builder);
				TAGMAP.put(entry.getKey(), nbt);
			} catch (Exception e) {
				EpicFightMod.LOGGER.warn("Error while deserializing weapon type datapack: " + entry.getKey());
				e.printStackTrace();
			}
		}
	}

	public static Function<Item, CapabilityItem.Builder> getOrThrow(String typeName) {
		ResourceLocation rl = new ResourceLocation(typeName);

		if (!PRESETS.containsKey(rl)) {
			throw new IllegalArgumentException("Can't find weapon type: " + rl);
		}

		return PRESETS.get(rl);
	}

	public static Function<Item, CapabilityItem.Builder> get(String typeName) {
		ResourceLocation rl = new ResourceLocation(typeName);
		return PRESETS.get(rl);
	}

	public static void register(ResourceLocation rl, CapabilityItem.Builder builder) {
		PRESETS.put(rl, (item) -> builder);
	}

	public static WeaponCapability.Builder deserializeWeaponCapabilityBuilder(CompoundNBT tag) {
		WeaponCapability.Builder builder = WeaponCapability.builder();

		if (!tag.contains("category") || StringUtil.isNullOrEmpty(tag.getString("category"))) {
			throw new IllegalArgumentException("Define weapon category.");
		}

		builder.category(WeaponCategory.ENUM_MANAGER.getOrThrow(tag.getString("category")));
		builder.collider(ColliderPreset.deserializeSimpleCollider(tag.getCompound("collider")));
		builder.canBePlacedOffhand(!tag.contains("usable_in_offhand") || tag.getBoolean("usable_in_offhand"));

		if (tag.contains("hit_particle")) {
			ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(tag.getString("hit_particle")));

			if (particleType == null) {
				EpicFightMod.LOGGER.warn("Can't find a particle type " + tag.getString("hit_particle"));
			} else if (!(particleType instanceof HitParticleType)) {
				EpicFightMod.LOGGER.warn(tag.getString("hit_particle") + " is not a hit particle type");
			} else {
				builder.hitParticle((HitParticleType)particleType);
			}
		}

		if (tag.contains("swing_sound")) {
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("swing_sound")));

			if (sound == null) {
				EpicFightMod.LOGGER.warn("Can't find a swing sound " + tag.getString("swing_sound"));
			} else {
				builder.swingSound(sound);
			}
		}

		if (tag.contains("hit_sound")) {
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("hit_sound")));

			if (sound == null) {
				EpicFightMod.LOGGER.warn("Can't find a hit sound " + tag.getString("hit_sound"));
			} else {
				builder.hitSound(sound);
			}
		}

		CompoundNBT combosTag = tag.getCompound("combos");

		for (String key : combosTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(key);
			ListNBT comboAnimations = combosTag.getList(key, Constants.NBT.TAG_STRING);
			StaticAnimation[] animArray = new StaticAnimation[comboAnimations.size()];

			for (int i = 0; i < comboAnimations.size(); i++) {
				animArray[i] = AnimationManager.getInstance().byKeyOrThrow(comboAnimations.getString(i));
			}

			builder.newStyleCombo(style, animArray);
		}

		CompoundNBT innateSkillsTag = tag.getCompound("innate_skills");

		for (String key : innateSkillsTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(key);

			//builder.innateSkill(style, (itemstack) -> SkillManager.getSkill(innateSkillsTag.getString(key)));
		}

		CompoundNBT livingmotionModifierTag = tag.getCompound("livingmotion_modifier");

		for (String sStyle : livingmotionModifierTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(sStyle);
			CompoundNBT styleAnimationTag = livingmotionModifierTag.getCompound(sStyle);

			for (String sLivingmotion : styleAnimationTag.getAllKeys()) {
				LivingMotion livingmotion = LivingMotion.ENUM_MANAGER.getOrThrow(sLivingmotion);
				StaticAnimation animation = AnimationManager.getInstance().byKeyOrThrow(styleAnimationTag.getString(sLivingmotion));

				builder.livingMotionModifier(style, livingmotion, animation);
			}
		}

		CompoundNBT stylesTag = tag.getCompound("styles");
		final List<Pair<Predicate<LivingEntityPatch<?>>, Style>> conditions = Lists.newArrayList();
		final Style defaultStyle = Style.ENUM_MANAGER.getOrThrow(stylesTag.getString("default"));

		for (INBT caseTag : stylesTag.getList("cases", Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT caseCompTag = (CompoundNBT)caseTag;
			List<Condition.EntityPatchCondition> conditionList = Lists.newArrayList();

			for (INBT offhandTag : caseCompTag.getList("conditions", Constants.NBT.TAG_COMPOUND)) {
				CompoundNBT offhandCompound = (CompoundNBT)offhandTag;
				Supplier<Condition.EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(new ResourceLocation(offhandCompound.getString("predicate")));
				Condition.EntityPatchCondition condition = conditionProvider.get();
				condition.read(offhandCompound);
				conditionList.add(condition);
			}

			conditions.add(Pair.of((entitypatch) -> {
				for (Condition.EntityPatchCondition condition : conditionList) {
					if (!condition.predicate(entitypatch)) {
						return false;
					}
				}

				return true;
			}, Style.ENUM_MANAGER.getOrThrow(caseCompTag.getString("style"))));
		}

		builder.styleProvider((entitypatch) -> {
			for (Pair<Predicate<LivingEntityPatch<?>>, Style> entry : conditions) {
				if (entry.first.test(entitypatch)) {
					return entry.second;
				}
			}

			return defaultStyle;
		});

		if (tag.contains("offhand_item_compatible_predicate")) {
			ListNBT offhandValidatorList = tag.getList("offhand_item_compatible_predicate", Constants.NBT.TAG_COMPOUND);
			List<Condition.EntityPatchCondition> conditionList = Lists.newArrayList();

			for (INBT offhandTag : offhandValidatorList) {
				CompoundNBT offhandCompound = (CompoundNBT)offhandTag;
				Supplier<Condition.EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(new ResourceLocation(offhandCompound.getString("predicate")));
				Condition.EntityPatchCondition condition = conditionProvider.get();
				condition.read(offhandCompound);
				conditionList.add(condition);
			}

			builder.weaponCombinationPredicator((entitypatch) -> {
				for (Condition.EntityPatchCondition condition : conditionList) {
					if (!condition.predicate(entitypatch)) {
						return false;
					}
				}

				return true;
			});
		}

		return builder;
	}

	public static int getTagCount() {
		return TAGMAP.size();
	}

	public static Stream<CompoundNBT> getWeaponTypeDataStream() {
		Stream<CompoundNBT> tagStream = TAGMAP.entrySet().stream().map((entry) -> {
			entry.getValue().putString("registry_name", entry.getKey().toString());
			return entry.getValue();
		});
		return tagStream;
	}

	public static Set<Map.Entry<ResourceLocation, Function<Item, CapabilityItem.Builder>>> entries() {
		return PRESETS.entrySet();
	}

	public static void clear() {
		PRESETS.clear();
		WeaponTypeReloadListener.registerDefaultWeaponTypes();
	}



	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSync packet) {
		if (packet.getType() == SPDatapackSync.Type.WEAPON_TYPE) {
			PRESETS.clear();
			registerDefaultWeaponTypes();

			for (CompoundNBT tag : packet.getTags()) {
				PRESETS.put(new ResourceLocation(tag.getString("registry_name")), (itemstack) -> deserializeWeaponCapabilityBuilder(tag));
			}

			ItemCapabilityReloadListener.weaponTypeProcessedCheck();
		}
	}

}