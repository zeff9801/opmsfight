
package yesman.epicfight.api.animation.types;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.client.animation.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.JointMask;
import yesman.epicfight.api.client.animation.JointMask.BindModifier;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.Layer.LayerType;
import yesman.epicfight.api.model.JsonModelLoader;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class StaticAnimation extends DynamicAnimation {
	protected final Map<AnimationProperty<?>, Object> properties = Maps.newHashMap();
	protected final StateSpectrum.Blueprint stateSpectrumBlueprint = new StateSpectrum.Blueprint();
	protected final ResourceLocation resourceLocation;
	protected final Model model;
	protected final int namespaceId;
	protected final int animationId;


	private final StateSpectrum stateSpectrum = new StateSpectrum();
	
	public StaticAnimation() {
		super(0.0F, false);
		this.namespaceId = -1;
		this.animationId = -1;
		this.resourceLocation = null;
		this.model = null;
	}

	public StaticAnimation(boolean repeatPlay, String path, Model model) {
		this(EpicFightOptions.GENERAL_ANIMATION_CONVERT_TIME, repeatPlay, path, model);
	}
	
	public StaticAnimation(float convertTime, boolean isRepeat, String path, Model model) {
		super(convertTime, isRepeat);
		
		AnimationManager animationManager = EpicFightMod.getInstance().animationManager;
		this.namespaceId = animationManager.getNamespaceHash();
		this.animationId = animationManager.getIdCounter();

		animationManager.getIdMap().put(this.animationId, this);
		this.resourceLocation = new ResourceLocation(animationManager.getModid(), "animmodels/animations/" + path);
		animationManager.getNameMap().put(new ResourceLocation(animationManager.getModid(), path), this);
		this.model = model;
	}
	
	public StaticAnimation(float convertTime, boolean repeatPlay, String path, Model model, boolean notRegisteredInAnimationManager) {
		super(convertTime, repeatPlay);
		this.namespaceId = -1;
		this.animationId = -1;
		this.resourceLocation = new ResourceLocation(EpicFightMod.getInstance().animationManager.getModid(), "animmodels/animations/" + path);
		this.model = model;
	}
	
	public static void load(IResourceManager resourceManager, StaticAnimation animation) {
		ResourceLocation extenderPath = new ResourceLocation(animation.resourceLocation.getNamespace(), animation.resourceLocation.getPath() + ".json");
		(new JsonModelLoader(resourceManager, extenderPath)).loadStaticAnimation(animation);
	}
	
	public static void loadBothSide(IResourceManager resourceManager, StaticAnimation animation) {
		ResourceLocation extenderPath = new ResourceLocation(animation.resourceLocation.getNamespace(), animation.resourceLocation.getPath() + ".json");
		(new JsonModelLoader(resourceManager, extenderPath)).loadStaticAnimationBothSide(animation);
	}
	
	public void loadAnimation(IResourceManager resourceManager) {
		try {
			int id = Integer.parseInt(this.resourceLocation.getPath().substring(22));
			StaticAnimation animation = EpicFightMod.getInstance().animationManager.findAnimationById(this.namespaceId, id);
			this.jointTransforms = animation.jointTransforms;
			this.setTotalTime(animation.totalTime);
		} catch (NumberFormatException e) {
			load(resourceManager, this);
		}
		
		this.onLoaded();
	}
	
	protected void onLoaded() {
		this.stateSpectrum.readFrom(this.stateSpectrumBlueprint);
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		this.getProperty(StaticAnimationProperty.EVENTS).ifPresent((events) -> {
			for (Event event : events) {
				if (event.time == Event.ON_BEGIN) {
					event.testAndExecute(entitypatch);
				}
			}
		});
	}
	@Override
	public boolean isStaticAnimation() {
		return true;
	}
	@Override
	public void end(LivingEntityPatch<?> entitypatch, boolean isEnd) {
		this.getProperty(StaticAnimationProperty.EVENTS).ifPresent((events) -> {
			for (Event event : events) {
				if (event.time == Event.ON_END) {
					event.testAndExecute(entitypatch);
				}
			}
		});
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		this.getProperty(StaticAnimationProperty.EVENTS).ifPresent((events) -> {
			AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
			
			if (player != null) {
				float prevElapsed = player.getPrevElapsedTime();
				float elapsed = player.getElapsedTime();
				
				for (Event event : events) {
					if (event.time != Event.ON_BEGIN && event.time != Event.ON_END) {
						if (event.time < prevElapsed || event.time >= elapsed) {
							continue;
						} else {
							event.testAndExecute(entitypatch);
						}
					}
				}
			}
		});
	}

	@Override
	public EntityState getState(LivingEntityPatch<?> entitypatch, float time) {
		return new EntityState(this.getStatesMap(entitypatch, time));
	}

	@Override
	public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, float time) {
		return this.stateSpectrum.getStateMap(entitypatch, time);
	}

	@Override
	public <T> T getState(EntityState.StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
		return this.stateSpectrum.getSingleState(stateFactor, entitypatch, time);
	}

		@Override
	public boolean isJointEnabled(LivingEntityPatch<?> entitypatch, String joint) {
		if (!super.isJointEnabled(entitypatch, joint)) {
			return false;
		} else {

            return this.getProperty(ClientAnimationProperties.JOINT_MASK).map((bindModifier) ->
					!bindModifier.isMasked(entitypatch.getCurrentLivingMotion(), joint)).orElse(true);
		}
	}
	@Override
	public TransformSheet getCoord() {
		return this.getProperty(AnimationProperty.MoveCoordFunctions.COORD).orElse(super.getCoord());
	}
	@Override
	public BindModifier getBindModifier(LivingEntityPatch<?> entitypatch, String joint) {
		return this.getProperty(ClientAnimationProperties.JOINT_MASK).map((jointMaskEntry) -> {
			List<JointMask> list = jointMaskEntry.getMask(entitypatch.getCurrentLivingMotion());
			int position = list.indexOf(JointMask.of(joint));
			
			if (position >= 0) {
				return list.get(position).getBindModifier();
			} else {
				return null;
			}
		}).orElse(null);
	}
	
	@Override
	public int getNamespaceId() {
		return this.namespaceId;
	}
	
	@Override
	public int getId() {
		return this.animationId;
	}
	
	public ResourceLocation getLocation() {
		return this.resourceLocation;
	}
	
	public Model getModel() {
		return this.model;
	}
	
	public boolean isBasicAttackAnimation() {
		return false;
	}
	
	@Override
	public float getPlaySpeed(LivingEntityPatch<?> entitypatch) {
		return this.getProperty(StaticAnimationProperty.PLAY_SPEED).orElse(1.0F);
	}
	
	@Override
	public String toString() {
		String classPath = this.getClass().toString();
		return classPath.substring(classPath.lastIndexOf(".") + 1) + " " + this.getLocation();
	}
	
	public <V> StaticAnimation addProperty(StaticAnimationProperty<V> propertyType, V value) {
		this.properties.put(propertyType, value);
		return this;
	}
	
	public StateSpectrum.Blueprint getStateSpectrumBP() {
		return this.stateSpectrumBlueprint;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
		return (Optional<V>) Optional.ofNullable(this.properties.get(propertyType));
	}
	
	@OnlyIn(Dist.CLIENT)
	public Layer.Priority getPriority() {
		return this.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.LOWEST);
	}
	
	@OnlyIn(Dist.CLIENT)
	public Layer.LayerType getLayerType() {
		return this.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(LayerType.BASE_LAYER);
	}

	public StaticAnimation newTimePair(float start, float end) {
		this.stateSpectrumBlueprint.newTimePair(start, end);
		return this;
	}

	public StaticAnimation newConditionalTimePair(Function<LivingEntityPatch<?>, Integer> condition, float start, float end) {
		this.stateSpectrumBlueprint.newConditionalTimePair(condition, start, end);
		return this;
	}

	public <T> StaticAnimation addState(EntityState.StateFactor<T> factor, T val) {
		this.stateSpectrumBlueprint.addState(factor, val);
		return this;
	}

	public <T> StaticAnimation removeState(EntityState.StateFactor<T> factor) {
		this.stateSpectrumBlueprint.removeState(factor);
		return this;
	}

	public <T> StaticAnimation addConditionalState(int metadata, EntityState.StateFactor<T> factor, T val) {
		this.stateSpectrumBlueprint.addConditionalState(metadata, factor, val);
		return this;
	}

	public <T> StaticAnimation addStateRemoveOld(EntityState.StateFactor<T> factor, T val) {
		this.stateSpectrumBlueprint.addStateRemoveOld(factor, val);
		return this;
	}

	public <T> StaticAnimation addStateIfNotExist(EntityState.StateFactor<T> factor, T val) {
		this.stateSpectrumBlueprint.addStateIfNotExist(factor, val);
		return this;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return this.resourceLocation;
	}

	public static class Event implements Comparable<Event> {
		public static final float ON_BEGIN = Float.MIN_VALUE;
		public static final float ON_END = Float.MAX_VALUE;
		final float time;
		final Side executionSide;
		final Consumer<LivingEntityPatch<?>> event;
		
		private Event(float time, Side executionSide, Consumer<LivingEntityPatch<?>> event) {
			this.time = time;
			this.executionSide = executionSide;
			this.event = event;
		}
		
		@Override
		public int compareTo(Event arg0) {
			if(this.time == arg0.time) {
				return 0;
			} else {
				return this.time > arg0.time ? 1 : -1;
			}
		}
		
		public void testAndExecute(LivingEntityPatch<?> entitypatch) {
			if (this.executionSide.predicate.test(entitypatch.isLogicalClient())) {
				this.event.accept(entitypatch);
			}
		}
		
		public static Event create(float time, Consumer<LivingEntityPatch<?>> event, Side isRemote) {
			return new Event(time, isRemote, event);
		}
		
		public enum Side {
			CLIENT((isLogicalClient) -> isLogicalClient), SERVER((isLogicalClient) -> !isLogicalClient), BOTH((isLogicalClient) -> true);
			
			Predicate<Boolean> predicate;
			
			Side(Predicate<Boolean> predicate) {
				this.predicate = predicate;
			}
		}
	}
}