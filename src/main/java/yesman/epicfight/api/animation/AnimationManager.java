package yesman.epicfight.api.animation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimationDataReader;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.api.utils.ClearableIdMapper;
import yesman.epicfight.api.utils.InstantiateInvoker;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class AnimationManager extends ReloadListener<Map<ResourceLocation, JsonElement>> {


	private static final AnimationManager INSTANCE = new AnimationManager();
	private static IResourceManager resourceManager = null;

	public static AnimationManager getInstance() {
		return INSTANCE;
	}

	private final Map<ResourceLocation, AnimationClip> animationClips = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> animationRegistry = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> userAnimations = Maps.newHashMap();
	private final ClearableIdMapper<StaticAnimation> animationIdMap = new ClearableIdMapper<> ();
	private String currentWorkingModid;


	private String modid;
	private int namespaceHash;
	private int counter = 0;

	public StaticAnimation byId(int animationId) {
		if (!this.animationIdMap.contains(animationId)) {
			throw new NoSuchElementException("No animation id " + animationId);
		}

		return this.animationIdMap.byId(animationId);
	}

	public StaticAnimation byKeyOrThrow(String resourceLocation) {
		return this.byKeyOrThrow(new ResourceLocation(resourceLocation));
	}

	public StaticAnimation byKeyOrThrow(ResourceLocation rl) {
		if (!this.animationRegistry.containsKey(rl)) {
			throw new NoSuchElementException("No animation with registry name " + rl);
		}

		return this.byKey(rl);
	}

	public StaticAnimation byKey(ResourceLocation rl) {
		return this.animationRegistry.get(rl);
	}


	public AnimationClip getStaticAnimationClip(StaticAnimation animation) {
		if (!this.animationClips.containsKey(animation.getLocation())) {
			animation.loadAnimation(resourceManager);
		}

		return this.animationClips.get(animation.getLocation());
	}

	public Map<ResourceLocation, StaticAnimation> getAnimations(Predicate<StaticAnimation> filter) {
		Map<ResourceLocation, StaticAnimation> filteredItems = this.animationRegistry.entrySet().stream().filter((entry) -> !this.userAnimations.containsKey(entry.getKey()) && filter.test(entry.getValue())).reduce(Maps.newHashMap(), (map, entry) -> {
			map.put(entry.getKey(), entry.getValue());
			return map;
		}, (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		});

		return ImmutableMap.copyOf(filteredItems);
	}


	public int registerAnimation(StaticAnimation staticAnimation) {
		if (this.currentWorkingModid != null) {
			if (this.animationRegistry.containsKey(staticAnimation.getRegistryName())) {
				EpicFightMod.LOGGER.error("Animation registration failed.");
				new IllegalStateException("[EpicFightMod] Animation with registry name " + staticAnimation.getRegistryName() + " already exists!").printStackTrace();
				return -1;
			}

			this.animationRegistry.put(staticAnimation.getRegistryName(), staticAnimation);
			int id = this.animationRegistry.size();
			this.animationIdMap.addMapping(staticAnimation, id);

			return id;
		}

		return -1;
	}
	public StaticAnimation refreshAnimation(StaticAnimation staticAnimation) {
		if (!this.animationRegistry.containsKey(staticAnimation.getRegistryName())) {
			throw new IllegalStateException("Animation refresh exception: No animation named " + staticAnimation.getRegistryName());
		}

		return this.animationRegistry.get(staticAnimation.getRegistryName());
	}

	public void loadAnimationClip(StaticAnimation animation, Function<StaticAnimation, AnimationClip> clipProvider) {
		if (!this.animationClips.containsKey(animation.getLocation())) {
			AnimationClip animationClip = clipProvider.apply(animation);
			this.animationClips.put(animation.getLocation(), animationClip);
		}
	}

	public void onFailed(StaticAnimation animation) {
		if (!this.animationClips.containsKey(animation.getLocation())) {
			this.animationClips.put(animation.getLocation(), AnimationClip.EMPTY_CLIP);
		}
	}

	public String workingModId() {
		return this.currentWorkingModid;
	}

	public static void readAnimationProperties(StaticAnimation animation) {
		if (resourceManager == null) return;
		ResourceLocation dataLocation = getAnimationDataFileLocation(animation.getLocation());

		try {
			Optional<IResource> resourceOptional = Optional.of(resourceManager.getResource(dataLocation));
			resourceOptional.ifPresent((rs) -> {
				ClientAnimationDataReader.readAndApply(animation, rs);
			});
		} catch (IOException e) {
			// Handle the exception (e.g., log it, rethrow it, etc.)
			System.err.println("Failed to get resource: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected Map<ResourceLocation, JsonElement> prepare(IResourceManager resourceManager, IProfiler profilerIn) {
		reloadResourceManager(resourceManager);
		Armatures.build(resourceManager);

		this.animationClips.clear();
		this.animationIdMap.clear();
		this.animationRegistry.clear();

		Map<String, Runnable> registryMap = Maps.newLinkedHashMap();
		ModLoader.get().postEvent(new AnimationRegistryEvent(registryMap));

		registryMap.forEach((key, value) -> {
            EpicFightMod.LOGGER.info("Register animations from {}", key);
			this.currentWorkingModid = key;
			value.run();
			this.currentWorkingModid = null;
		});

		return prepareAnimationMap(resourceManager);
	}

	private Map<ResourceLocation, JsonElement> prepareAnimationMap(IResourceManager resourceManager) {
        // Your logic to populate the map goes here
		// For example, loading animation JSON elements from the resource manager
		return Maps.newHashMap();
	}


	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManager, IProfiler profilerIn) {
		final Map<ResourceLocation, StaticAnimation> registeredAnimation = Maps.newHashMap();
		this.animationRegistry.values().forEach(a1 -> a1.getClipHolders().forEach((a2) -> registeredAnimation.put(a2.getRegistryName(), a2)));

		/**
		 * Load animations that are not registered from {@link AnimationRegistryEvent}
		 * Reads from Resource Pack in physical client, Datapack in physical server.
		 */
		objectIn.entrySet().stream().filter((entry) -> !registeredAnimation.containsKey(entry.getKey()) && !entry.getKey().getPath().contains("/data/"))
				.sorted((e1, e2) -> e1.getKey().toString().compareTo(e2.getKey().toString()))
				.forEach((entry) -> {
					if (!entry.getKey().getNamespace().equals(this.currentWorkingModid)) {
						this.currentWorkingModid = entry.getKey().getNamespace();
					}

					try {
						this.readAnimationFromJson(entry.getKey(), entry.getValue().getAsJsonObject());
					} catch (Exception e) {
						EpicFightMod.LOGGER.error("Failed to load User animation " + entry.getKey() + " because of " + e + ". Skipped.");
						e.printStackTrace();
					}
				});

		//SkillManager.reloadAllSkillsAnimations();

		this.animationRegistry.values().stream().reduce(Lists.<StaticAnimation>newArrayList(), (list, anim) -> {
			list.addAll(anim.getClipHolders());
			return list;
		}, (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		}).forEach((animation) -> {
			animation.postInit();

			if (EpicFightMod.isPhysicalClient()) {
				AnimationManager.readAnimationProperties(animation);
			}
		});
	}

	public static ResourceLocation getAnimationDataFileLocation(ResourceLocation location) {
		int splitIdx = location.getPath().lastIndexOf('/');

		if (splitIdx < 0) {
			splitIdx = 0;
		}

		return new ResourceLocation(location.getNamespace(), String.format("%s/data%s", location.getPath().substring(0, splitIdx), location.getPath().substring(splitIdx)));
	}

	private static void reloadResourceManager(IResourceManager pResourceManager) {
		if (resourceManager != pResourceManager) {
			resourceManager = pResourceManager;
		}
	}

	public static IResourceManager getAnimationResourceManager() {
		return EpicFightMod.isPhysicalClient() ? Minecraft.getInstance().getResourceManager() : resourceManager;
	}

	/**************************************************
	 * User-animation loader
	 **************************************************/
	@SuppressWarnings({ "deprecation" })
	private void readAnimationFromJson(ResourceLocation rl, JsonObject json) throws Exception {
		JsonElement constructorElement = json.get("constructor");

		if (constructorElement == null) {
			throw new IllegalStateException("No constructor information has provided in User animation " + rl);
		}

		JsonObject constructorObject = constructorElement.getAsJsonObject();
		String invocationCommand = constructorObject.get("invocation_command").getAsString();
		StaticAnimation animation = InstantiateInvoker.invoke(invocationCommand, StaticAnimation.class).getResult();
		this.userAnimations.put(animation.getRegistryName(), animation);

		JsonElement propertiesElement = json.getAsJsonObject().get("properties");

		if (propertiesElement != null) {
			JsonObject propertiesObject = propertiesElement.getAsJsonObject();

			for (Map.Entry<String, JsonElement> entry : propertiesObject.entrySet()) {
				AnimationProperty<?> propertyKey = AnimationProperty.getSerializableProperty(entry.getKey());
				Object value = propertyKey.parseFrom(entry.getValue());
				animation.addPropertyUnsafe(propertyKey, value);
			}
		}
	}

}