package yesman.epicfight.api.animation;

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
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class AnimationManager extends ReloadListener<Map<Integer, Map<Integer, StaticAnimation>>> {

	private final Map<Integer, Map<Integer, StaticAnimation>> animationById = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> animationByName = Maps.newHashMap();


	private final Map<ResourceLocation, AnimationClip> animationClips = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> animationRegistry = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> userAnimations = Maps.newHashMap();
	private final ClearableIdMapper<StaticAnimation> animationIdMap = new ClearableIdMapper<> ();
	private String currentWorkingModid;



	private String modid;
	private int namespaceHash;
	private int counter = 0;
	private static final AnimationManager INSTANCE = new AnimationManager();
	private static IResourceManager resourceManager = null;

	public static AnimationManager getInstance() {
		return INSTANCE;
	}

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
		if (!this.animationByName.containsKey(rl)) {
			throw new NoSuchElementException("No animation with registry name " + rl);
		}

		return this.byKey(rl);
	}

	public StaticAnimation byKey(ResourceLocation rl) {
		return this.animationByName.get(rl);
	}

	public StaticAnimation findAnimationByPath(String resourceLocation) {
		ResourceLocation rl = new ResourceLocation(resourceLocation);

		if (this.animationByName.containsKey(rl)) {
			return this.animationByName.get(rl);
		}

		throw new IllegalArgumentException("Unable to find animation: " + rl);
	}

	public AnimationClip getStaticAnimationClip(StaticAnimation animation) {
		if (!this.animationClips.containsKey(animation.getLocation())) {
			animation.loadAnimation(resourceManager);
		}

		return this.animationClips.get(animation.getLocation());
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

	public void registerAnimations() {
		Map<String, Runnable> registryMap = Maps.newHashMap();
		ModLoader.get().postEvent(new AnimationRegistryEvent(registryMap));

		registryMap.forEach((key, value) -> {
            this.modid = key;
            this.namespaceHash = this.modid.hashCode();
            this.animationById.put(this.namespaceHash, Maps.newHashMap());
            this.counter = 0;
            value.run();
        });
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

	@Override
	protected Map<Integer, Map<Integer, StaticAnimation>> prepare(IResourceManager resourceManager, IProfiler profilerIn) {

		reloadResourceManager(resourceManager);

		this.animationClips.clear();
		this.animationIdMap.clear();
		this.animationRegistry.clear();
		//Armatures.build(resourceManager);
		Animations.buildClient();

		Map<String, Runnable> registryMap = Maps.newLinkedHashMap();
		ModLoader.get().postEvent(new AnimationRegistryEvent(registryMap));

		registryMap.entrySet().forEach((entry) -> {
			EpicFightMod.LOGGER.info("Register animations from " + entry.getKey());
			this.currentWorkingModid = entry.getKey();
			entry.getValue().run();

			this.currentWorkingModid = null;
		});

		return this.animationById;
	}

	@Override
	protected void apply(Map<Integer, Map<Integer, StaticAnimation>> objectIn, IResourceManager resourceManager, IProfiler profilerIn) {
		final Map<ResourceLocation, StaticAnimation> registeredAnimation = Maps.newHashMap();
		this.animationRegistry.values().forEach(a1 -> a1.getClipHolders().forEach((a2) -> registeredAnimation.put(a2.getRegistryName(), a2)));

		//SkillManager.reloadAllSkillsAnimations();
		objectIn.values().forEach((map) -> {
			map.values().forEach((animation) -> {
				animation.loadAnimation(resourceManager);
			});
		});

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

	public String getModid() {
		return this.modid;
	}

	public int getNamespaceHash() {
		return this.namespaceHash;
	}

	public int getIdCounter() {
		return this.counter++;
	}

	public Map<Integer, StaticAnimation> getIdMap() {
		return this.animationById.get(this.namespaceHash);
	}

	public Map<ResourceLocation, StaticAnimation> getNameMap() {
		return this.animationByName;
	}

	public String workingModId() {
		return this.currentWorkingModid;
	}
}