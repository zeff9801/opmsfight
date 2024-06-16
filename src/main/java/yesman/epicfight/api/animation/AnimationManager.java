package yesman.epicfight.api.animation;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.AnimationDataReader;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;

public class AnimationManager extends ReloadListener<Map<Integer, Map<Integer, StaticAnimation>>> {
	private final Map<Integer, Map<Integer, StaticAnimation>> animationById = Maps.newHashMap();
	private final Map<ResourceLocation, StaticAnimation> animationByName = Maps.newHashMap();
	private String modid;
	private int namespaceHash;
	private int counter = 0;
	private static final AnimationManager INSTANCE = new AnimationManager();
	private static IResourceManager resourceManager = null;

	public static AnimationManager getInstance() {
		return INSTANCE;
	}

    public StaticAnimation byId(int namespaceId, int animationId) {

		if (this.animationById.containsKey(namespaceId)) {
			Map<Integer, StaticAnimation> map = this.animationById.get(namespaceId);
			if (map.containsKey(animationId)) {
				return map.get(animationId);
			}
		}
		throw new IllegalArgumentException("Unable to find animation. id: " + animationId + ", namespcae hash: " + namespaceId);
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

	public void loadAnimationsInit(IResourceManager resourceManager) {
		this.animationById.values().forEach((map) -> {
			map.values().forEach((animation) -> {
				animation.loadAnimation(resourceManager);
				this.setAnimationProperties(resourceManager, animation);
			});
		});
	}

	@Override
	protected Map<Integer, Map<Integer, StaticAnimation>> prepare(IResourceManager resourceManager, IProfiler profilerIn) {
		if (EpicFightMod.isPhysicalClient()) {
			this.animationById.values().forEach((map) -> {
				map.values().forEach((animation) -> {
					this.setAnimationProperties(resourceManager, animation);
				});
			});
		}
		Animations.buildClient();

		return this.animationById;
	}

	@Override
	protected void apply(Map<Integer, Map<Integer, StaticAnimation>> objectIn, IResourceManager resourceManager, IProfiler profilerIn) {
		objectIn.values().forEach((map) -> {
			map.values().forEach((animation) -> {
				animation.loadAnimation(resourceManager);
			});
		});
	}

	private void setAnimationProperties(IResourceManager resourceManager, StaticAnimation animation) {
		if (resourceManager == null) {
			return;
		}

		ResourceLocation location = animation.getLocation();
		String path = location.getPath();
		int last = location.getPath().lastIndexOf('/');

		if (last > 0) {
			ResourceLocation dataLocation = new ResourceLocation(location.getNamespace(), String.format("%s/data%s.json", path.substring(0, last), path.substring(last)));

			if (resourceManager.hasResource(dataLocation)) {
				try {
					AnimationDataReader.readAndApply(animation, resourceManager, resourceManager.getResource(dataLocation));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
}