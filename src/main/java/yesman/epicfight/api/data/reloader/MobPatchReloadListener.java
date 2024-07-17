package yesman.epicfight.api.data.reloader;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.*;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.*;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.Behavior;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.BehaviorPredicate;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.BehaviorSeries;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MobPatchReloadListener extends JsonReloadListener {
	public static final String DIRECTORY = "epicfight_mobpatch";
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Map<EntityType<?>, CompoundNBT> TAGMAP = Maps.newHashMap();
	private static final Map<EntityType<?>, AbstractMobPatchProvider> MOB_PATCH_PROVIDERS = Maps.newHashMap();

	public MobPatchReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected Map<ResourceLocation, JsonElement> prepare(IResourceManager resourceManager, IProfiler profileIn) {
		MOB_PATCH_PROVIDERS.clear();
		TAGMAP.clear();
		return super.prepare(resourceManager, profileIn);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManager, IProfiler profilerIn) {
		for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
			ResourceLocation rl = entry.getKey();
			String pathString = rl.getPath();
			ResourceLocation registryName = new ResourceLocation(rl.getNamespace(), pathString);

			if (!ForgeRegistries.ENTITIES.containsKey(registryName)) {
				new NoSuchElementException("Mob Patch Exception: No Entity named " + registryName).printStackTrace();
				continue;
			}

			EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(registryName);
			CompoundNBT tag = null;

			try {
				tag = JsonToNBT.parseTag(entry.getValue().toString());
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}

			MOB_PATCH_PROVIDERS.put(entityType, deserialize(entityType, tag, false, resourceManager));

			EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
			TAGMAP.put(entityType, filterClientData(tag));

			if (EpicFightMod.isPhysicalClient()) {
				ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"), tag);
			}
		}
	}

	public static abstract class AbstractMobPatchProvider {
		public abstract EntityPatch<?> get(Entity entity);
	}

	public static class NullPatchProvider extends AbstractMobPatchProvider {
		@Override
		public EntityPatch<?> get(Entity entity) {
			return null;
		}
	}

	public static class BranchProvider extends AbstractMobPatchProvider {
		protected List<Pair<EpicFightPredicates<Entity>, AbstractMobPatchProvider>> providers = Lists.newArrayList();
		protected AbstractMobPatchProvider defaultProvider;

		@Override
		public EntityPatch<?> get(Entity entity) {
			for (Pair<EpicFightPredicates<Entity>, AbstractMobPatchProvider> provider : this.providers) {
				if (provider.getFirst().test(entity)) {
					return provider.getSecond().get(entity);
				}
			}

			return this.defaultProvider.get(entity);
		}
	}

	public static class MobPatchPresetProvider extends AbstractMobPatchProvider {
		protected final Function<Entity, Supplier<EntityPatch<?>>> presetProvider;

		public MobPatchPresetProvider(Function<Entity, Supplier<EntityPatch<?>>> presetProvider) {
			this.presetProvider = presetProvider;
		}

		@Override
		public EntityPatch<?> get(Entity entity) {
			return this.presetProvider.apply(entity).get();
		}
	}

	public static class CustomHumanoidMobPatchProvider extends CustomMobPatchProvider {
		protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> humanoidCombatBehaviors;
		protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> humanoidWeaponMotions;

		@SuppressWarnings("rawtypes")
		@Override
		public EntityPatch<?> get(Entity entity) {
			return new CustomHumanoidMobPatch(this.faction, this);
		}

		public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> getHumanoidWeaponMotions() {
			return this.humanoidWeaponMotions;
		}

		public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> getHumanoidCombatBehaviors() {
			return this.humanoidCombatBehaviors;
		}
	}

	public static class CustomMobPatchProvider extends AbstractMobPatchProvider {
		protected CombatBehaviors.Builder<?> combatBehaviorsBuilder;
		protected List<Pair<LivingMotion, StaticAnimation>> defaultAnimations;
		protected Map<StunType, StaticAnimation> stunAnimations;
		protected Object2DoubleMap<Attribute> attributeValues;
		protected Faction faction;
		protected double chasingSpeed = 1.0D;
		protected float scale;
		protected SoundEvent swingSound = EpicFightSounds.WHOOSH;
		protected SoundEvent hitSound = EpicFightSounds.BLUNT_HIT;
		protected HitParticleType hitParticle = EpicFightParticles.HIT_BLUNT.get();

		@Override
		@SuppressWarnings("rawtypes")
		public EntityPatch<?> get(Entity entity) {
			return new CustomMobPatch(this.faction, this);
		}

		public CombatBehaviors.Builder<?> getCombatBehaviorsBuilder() {
			return this.combatBehaviorsBuilder;
		}

		public List<Pair<LivingMotion, StaticAnimation>> getDefaultAnimations() {
			return this.defaultAnimations;
		}

		public Map<StunType, StaticAnimation> getStunAnimations() {
			return this.stunAnimations;
		}

		public Object2DoubleMap<Attribute> getAttributeValues() {
			return this.attributeValues;
		}

		public double getChasingSpeed() {
			return this.chasingSpeed;
		}

		public float getScale() {
			return this.scale;
		}

		public SoundEvent getSwingSound() {
			return this.swingSound;
		}

		public SoundEvent getHitSound() {
			return this.hitSound;
		}

		public HitParticleType getHitParticle() {
			return this.hitParticle;
		}
	}

	public static AbstractMobPatchProvider deserialize(EntityType<?> entityType, CompoundNBT tag, boolean clientSide, IResourceManager resourceManager) {
		AbstractMobPatchProvider provider = null;
		int i = 0;
		boolean hasBranch = tag.contains(String.format("branch_%d", i));

		if (hasBranch) {
			provider = new BranchProvider();
			((BranchProvider)provider).defaultProvider = deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
		} else {
			provider = deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
		}

		while (hasBranch) {
			CompoundNBT branchTag = tag.getCompound(String.format("branch_%d", i));
			((BranchProvider)provider).providers.add(Pair.of(deserializeBranchPredicate(branchTag.getCompound("condition")), deserialize(entityType, branchTag, clientSide, resourceManager)));
			hasBranch = tag.contains(String.format("branch_%d", ++i));
		}

		return provider;
	}

	public static EpicFightPredicates<Entity> deserializeBranchPredicate(CompoundNBT tag) {
		String predicateType = tag.getString("predicate");
		EpicFightPredicates<Entity> predicate = null;

        if (predicateType.equals("has_tags")) {
            if (!tag.contains("tags", 9)) {
				EpicFightMod.LOGGER.info("[Custom Entity Error] can't find a proper argument for %s. [name: %s, type: %s]".formatted("has_tags", "tags", "string list"));
            }
            predicate = new EpicFightPredicates.HasTag(tag.getList("tags", 8));
        }

		if (predicate == null) {
		throw new IllegalArgumentException("[Custom Entity Error] No predicate type: " + predicateType);
	}

		return predicate;
}
	public static AbstractMobPatchProvider deserializeMobPatchProvider(EntityType<?> entityType, CompoundNBT tag, boolean clientSide, IResourceManager resourceManager) {
		boolean disabled = tag.contains("disabled") && tag.getBoolean("disabled");

		if (disabled) {
			return new NullPatchProvider();
		} else if (tag.contains("preset")) {
			String presetName = tag.getString("preset");
			Function<Entity, Supplier<EntityPatch<?>>> preset = EntityPatchProvider.get(presetName);
			EntityType<?> presetEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(presetName));

			Armatures.registerEntityTypeArmature(entityType, Armatures.getRegistry(presetEntityType));

			MobPatchPresetProvider provider = new MobPatchPresetProvider(preset);

			return provider;
		} else {
			boolean humanoid = tag.getBoolean("isHumanoid");
			CustomMobPatchProvider provider = humanoid ? new CustomHumanoidMobPatchProvider() : new CustomMobPatchProvider();
			provider.attributeValues = deserializeAttributes(tag.getCompound("attributes"));
			ResourceLocation modelLocation = new ResourceLocation(tag.getString("model"));
			ResourceLocation armatureLocation = new ResourceLocation(tag.getString("armature"));

			if (EpicFightMod.isPhysicalClient()) {
				Meshes.getOrCreateAnimatedMesh(Minecraft.getInstance().getResourceManager(), modelLocation, humanoid ? AnimatedMesh::new : HumanoidMesh::new);
			}

			Armature armature = Armatures.getOrCreateArmature(resourceManager, armatureLocation, humanoid ? Armature::new : HumanoidArmature::new);
			Armatures.registerEntityTypeArmature(entityType, armature);

			provider.defaultAnimations = deserializeDefaultAnimations(tag.getCompound("default_livingmotions"));
			provider.faction = Faction.valueOf(tag.getString("faction").toUpperCase(Locale.ROOT));
			provider.scale = tag.getCompound("attributes").contains("scale") ? (float)tag.getCompound("attributes").getDouble("scale") : 1.0F;

			if (tag.contains("swing_sound")) {
				provider.swingSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("swing_sound")));
			}

			if (tag.contains("hit_sound")) {
				provider.hitSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(tag.getString("hit_sound")));
			}

			if (tag.contains("hit_particle")) {
				provider.hitParticle = (HitParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(tag.getString("hit_particle")));
			}

			if (!clientSide) {
				provider.stunAnimations = deserializeStunAnimations(tag.getCompound("stun_animations"));

				if (tag.getCompound("attributes").contains("chasing_speed")) {
					provider.chasingSpeed = tag.getCompound("attributes").getDouble("chasing_speed");
				}

				if (humanoid) {
					CustomHumanoidMobPatchProvider humanoidProvider = (CustomHumanoidMobPatchProvider)provider;
					humanoidProvider.humanoidCombatBehaviors = deserializeHumanoidCombatBehaviors(tag.getList("combat_behavior", 10));
					humanoidProvider.humanoidWeaponMotions = deserializeHumanoidWeaponMotions(tag.getList("humanoid_weapon_motions", 10));
				} else {
					provider.combatBehaviorsBuilder = deserializeCombatBehaviorsBuilder(tag.getList("combat_behavior", 10));
				}
			}

			return provider;
		}
	}

	public static Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> deserializeHumanoidCombatBehaviors(ListNBT tag) {
		Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> combatBehaviorsMapBuilder = Maps.newHashMap();

		for (int i = 0; i < tag.size(); i++) {
			CompoundNBT combatBehavior = tag.getCompound(i);
			ListNBT categories = combatBehavior.getList("weapon_categories", 8);
			Style style = Style.ENUM_MANAGER.getOrThrow(combatBehavior.getString("style"));
			CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = deserializeCombatBehaviorsBuilder(combatBehavior.getList("behavior_series", 10));

			for (int j = 0; j < categories.size(); j++) {
				WeaponCategory category = WeaponCategory.ENUM_MANAGER.getOrThrow(categories.getString(j));
				combatBehaviorsMapBuilder.computeIfAbsent(category, (key) -> Maps.newHashMap());
				combatBehaviorsMapBuilder.get(category).put(style, builder);
			}
		}

		return combatBehaviorsMapBuilder;
	}

	public static List<Pair<LivingMotion, StaticAnimation>> deserializeDefaultAnimations(CompoundNBT defaultLivingmotions) {
		List<Pair<LivingMotion, StaticAnimation>> defaultAnimations = Lists.newArrayList();

		for (String key : defaultLivingmotions.getAllKeys()) {
			String animation = defaultLivingmotions.getString(key);
			defaultAnimations.add(Pair.of(LivingMotion.ENUM_MANAGER.getOrThrow(key), AnimationManager.getInstance().byKeyOrThrow(animation)));
		}

		return defaultAnimations;
	}

	public static Map<StunType, StaticAnimation> deserializeStunAnimations(CompoundNBT tag) {
		Map<StunType, StaticAnimation> stunAnimations = Maps.newHashMap();

		for (StunType stunType : StunType.values()) {
			String lowerCaseName = tag.getString(stunType.name().toLowerCase(Locale.ROOT));

			if (!StringUtil.isNullOrEmpty(lowerCaseName)) {
				stunAnimations.put(stunType, AnimationManager.getInstance().byKeyOrThrow(lowerCaseName));
			}
		}

		return stunAnimations;
	}

	public static Object2DoubleMap<Attribute> deserializeAttributes(CompoundNBT tag) {
		Object2DoubleMap<Attribute> attributes = new Object2DoubleOpenHashMap<>();
		attributes.put(EpicFightAttributes.IMPACT.get(), tag.contains("impact", Constants.NBT.TAG_DOUBLE) ? tag.getDouble("impact") : 0.5D);
		attributes.put(EpicFightAttributes.ARMOR_NEGATION.get(), tag.contains("armor_negation", Constants.NBT.TAG_DOUBLE) ? tag.getDouble("armor_negation") : 0.0D);
		attributes.put(EpicFightAttributes.MAX_STRIKES.get(), (tag.contains("max_strikes", Constants.NBT.TAG_INT) ? tag.getInt("max_strikes") : 1));
		attributes.put(EpicFightAttributes.STUN_ARMOR.get(), (tag.contains("stun_armor", Constants.NBT.TAG_DOUBLE) ? tag.getDouble("stun_armor") : 0.0D));

		if (tag.contains("attack_damage", Constants.NBT.TAG_DOUBLE)) {
			attributes.put(Attributes.ATTACK_DAMAGE, tag.getDouble("attack_damage"));
		}

		return attributes;
	}

	public static Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> deserializeHumanoidWeaponMotions(ListNBT tag) {
		Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> map = Maps.newHashMap();

		for (int i = 0; i < tag.size(); i++) {
			ImmutableSet.Builder<Pair<LivingMotion, StaticAnimation>> motions = ImmutableSet.builder();
			CompoundNBT weaponMotionTag = tag.getCompound(i);
			Style style = Style.ENUM_MANAGER.getOrThrow(weaponMotionTag.getString("style"));
			CompoundNBT motionsTag = weaponMotionTag.getCompound("livingmotions");

			for (String key : motionsTag.getAllKeys()) {
				motions.add(Pair.of(LivingMotion.ENUM_MANAGER.getOrThrow(key), AnimationManager.getInstance().byKeyOrThrow(motionsTag.getString(key))));
			}

			INBT weponTypeTag = weaponMotionTag.get("weapon_categories");

			if (weponTypeTag instanceof StringNBT) {
				WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.getOrThrow(weponTypeTag.getAsString());
				if (!map.containsKey(weaponCategory)) {
					map.put(weaponCategory, Maps.newHashMap());
				}
				map.get(weaponCategory).put(style, motions.build());

			} else if (weponTypeTag instanceof ListNBT weponTypesTag) {

				for (int j = 0; j < weponTypesTag.size(); j++) {
					WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.getOrThrow(weponTypesTag.getString(j));
					if (!map.containsKey(weaponCategory)) {
						map.put(weaponCategory, Maps.newHashMap());
					}
					map.get(weaponCategory).put(style, motions.build());
				}
			}
		}

		return map;
	}

	public static <T extends MobPatch<?>> CombatBehaviors.Builder<T> deserializeCombatBehaviorsBuilder(ListNBT tag) {
		CombatBehaviors.Builder<T> builder = CombatBehaviors.builder();

		for (int i = 0; i < tag.size(); i++) {
			CompoundNBT behaviorSeries = tag.getCompound(i);
			float weight = (float)behaviorSeries.getDouble("weight");
			int cooldown = behaviorSeries.contains("cooldown") ? behaviorSeries.getInt("cooldown") : 0;
			boolean canBeInterrupted = behaviorSeries.contains("canBeInterrupted") && behaviorSeries.getBoolean("canBeInterrupted");
			boolean looping = behaviorSeries.contains("looping") && behaviorSeries.getBoolean("looping");
			ListNBT behaviorList = behaviorSeries.getList("behaviors", 10);
			BehaviorSeries.Builder<T> behaviorSeriesBuilder = BehaviorSeries.builder();
			behaviorSeriesBuilder.weight(weight).cooldown(cooldown).canBeInterrupted(canBeInterrupted).looping(looping);

			for (int j = 0; j < behaviorList.size(); j++) {
				Behavior.Builder<T> behaviorBuilder = Behavior.builder();
				CompoundNBT behavior = behaviorList.getCompound(j);
				StaticAnimation animation = AnimationManager.getInstance().byKeyOrThrow(behavior.getString("animation"));
				ListNBT conditionList = behavior.getList("conditions", 10);
				behaviorBuilder.animationBehavior(animation);

				for (int k = 0; k < conditionList.size(); k++) {
					CompoundNBT condition = conditionList.getCompound(k);
					Condition<T> predicate = deserializeBehaviorPredicate(condition.getString("predicate"), condition);
					behaviorBuilder.predicate(predicate);
				}

				behaviorSeriesBuilder.nextBehavior(behaviorBuilder);
			}

			builder.newBehaviorSeries(behaviorSeriesBuilder);
		}

		return builder;
	}

	public static <T extends MobPatch<?>> Condition<T> deserializeBehaviorPredicate(String type, CompoundNBT args) {
		ResourceLocation rl;

		if (type.contains(":")) {
			rl = new ResourceLocation(type);
		} else {
			rl = new ResourceLocation(EpicFightMod.MODID, type);
		}

		Supplier<Condition<T>> predicateProvider = EpicFightConditions.getConditionOrNull(rl);
		Condition<T> condition = predicateProvider.get();
		condition.read(args);

		return condition;
	}

	public static CompoundNBT filterClientData(CompoundNBT tag) {
		CompoundNBT clientTag = new CompoundNBT();
		int i = 0;
		boolean hasBranch = tag.contains(String.format("branch_%d", i));

		while (hasBranch) {
			CompoundNBT branchTag = tag.getCompound(String.format("branch_%d", i));
			CompoundNBT copiedTag = new CompoundNBT();
			extractBranch(copiedTag, branchTag);
			clientTag.put(String.format("branch_%d", i), copiedTag);
			hasBranch = tag.contains(String.format("branch_%d", ++i));
		}

		extractBranch(clientTag, tag);

		return clientTag;
	}

	public static CompoundNBT extractBranch(CompoundNBT extract, CompoundNBT original) {
		if (original.contains("disabled") && original.getBoolean("disabled")) {
			extract.put("disabled", original.get("disabled"));
		} else if (original.contains("preset")) {
			extract.put("preset", original.get("preset"));
		} else {
			extract.put("model", original.get("model"));
			extract.put("armature", original.get("armature"));
			extract.putBoolean("isHumanoid", original.contains("isHumanoid") ? original.getBoolean("isHumanoid") : false);
			extract.put("renderer", original.get("renderer"));
			extract.put("faction", original.get("faction"));
			extract.put("default_livingmotions", original.get("default_livingmotions"));
			extract.put("attributes", original.get("attributes"));
		}

		return extract;
	}

	public static Stream<CompoundNBT> getDataStream() {
		Stream<CompoundNBT> tagStream = TAGMAP.entrySet().stream().map((entry) -> {
			entry.getValue().putString("id", ForgeRegistries.ENTITIES.getKey(entry.getKey()).toString());
			return entry.getValue();
		});

		return tagStream;
	}

	public static int getTagCount() {
		return TAGMAP.size();
	}

	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSync packet) {
		for (CompoundNBT tag : packet.getTags()) {
			boolean disabled = false;

			if (tag.contains("disabled")) {
				disabled = tag.getBoolean("disabled");
			}

			EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(tag.getString("id")));
			MOB_PATCH_PROVIDERS.put(entityType, deserialize(entityType, tag, true, Minecraft.getInstance().getResourceManager()));
			EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));

			if (!disabled) {
				if (tag.contains("preset")) {
					Armatures.registerEntityTypeArmature(entityType, tag.getString("preset"));
				} else {
					Minecraft mc = Minecraft.getInstance();
					ResourceLocation armatureLocation = new ResourceLocation(tag.getString("armature"));
					boolean humanoid = tag.getBoolean("isHumanoid");
					Armature armature = Armatures.getOrCreateArmature(mc.getResourceManager(), armatureLocation, humanoid ? Armature::new : HumanoidArmature::new);
					Armatures.registerEntityTypeArmature(entityType, armature);
				}

				ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"), tag);
			}
		}
	}
}