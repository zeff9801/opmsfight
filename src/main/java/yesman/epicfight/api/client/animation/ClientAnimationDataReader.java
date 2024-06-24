package yesman.epicfight.api.client.animation;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.IResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.property.GsonHelper;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.animation.property.LayerInfo;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.main.EpicFightMod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientAnimationDataReader {
	public static final ClientAnimationDataReader.Deserializer DESERIALIZER = new ClientAnimationDataReader.Deserializer();
	private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ClientAnimationDataReader.class, DESERIALIZER).create();
	private static final TypeToken<ClientAnimationDataReader> TYPE = new TypeToken<ClientAnimationDataReader>() {};
	private final LayerInfo layerInfo;
	private final LayerInfo multilayerInfo;
	private final List<TrailInfo> trailInfo;

	public static void readAndApply(StaticAnimation animation, IResource iresource) {
		InputStream inputstream = null;

        inputstream = iresource.getInputStream();

        assert inputstream != null;
		readAndApply(animation, inputstream);
	}

	public static void readAndApply(StaticAnimation animation, InputStream resourceReader) {
		Reader reader = new InputStreamReader(resourceReader, StandardCharsets.UTF_8);
		ClientAnimationDataReader propertySetter = GsonHelper.fromJson(GSON, reader, TYPE);
		propertySetter.applyClientData(animation);
	}
	public void applyClientData(StaticAnimation animation) {
		if (this.layerInfo != null) {
			if (this.layerInfo.jointMaskEntry.isValid()) {
				animation.addProperty(ClientAnimationProperties.JOINT_MASK, this.layerInfo.jointMaskEntry);
			}

			animation.addProperty(ClientAnimationProperties.LAYER_TYPE, this.layerInfo.layerType);
			animation.addProperty(ClientAnimationProperties.PRIORITY, this.layerInfo.priority);
		}

		if (this.multilayerInfo != null) {
			//StaticAnimation multilayerAnimation = new StaticAnimation(animation.getLocation(), animation.getConvertTime(), animation.isRepeat(), animation.getRegistryName().toString() + "_multilayer", animation.getModel().getArmature(), true);
			StaticAnimation multilayerAnimation = new StaticAnimation(animation.getConvertTime(),animation.isRepeat(),animation.getRegistryName().toString() + "_multilayer",animation.getModel(),true);

			if (this.multilayerInfo.jointMaskEntry.isValid()) {
				multilayerAnimation.addProperty(ClientAnimationProperties.JOINT_MASK, this.multilayerInfo.jointMaskEntry);
			}

			multilayerAnimation.addProperty(ClientAnimationProperties.LAYER_TYPE, this.multilayerInfo.layerType);
			multilayerAnimation.addProperty(ClientAnimationProperties.PRIORITY, this.multilayerInfo.priority);
			multilayerAnimation.addProperty(AnimationProperty.StaticAnimationProperty.ELAPSED_TIME_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
				Layer baseLayer = entitypatch.getClientAnimator().baseLayer;

				if (baseLayer.animationPlayer.getAnimation().getRealAnimation() != animation) {
					return Pair.of(prevElapsedTime, elapsedTime);
				}

				if (!self.isStaticAnimation() && baseLayer.animationPlayer.getAnimation().isStaticAnimation()) {
					return Pair.of(prevElapsedTime + speed, elapsedTime + speed);
				}

				return Pair.of(baseLayer.animationPlayer.getPrevElapsedTime(), baseLayer.animationPlayer.getElapsedTime());
			});

			animation.addProperty(ClientAnimationProperties.MULTILAYER_ANIMATION, multilayerAnimation);
		}

		if (!this.trailInfo.isEmpty()) {
			animation.addProperty(ClientAnimationProperties.TRAIL_EFFECT, this.trailInfo);
		}
	}
	private ClientAnimationDataReader(LayerInfo compositeLayerInfo, LayerInfo layerInfo, List<TrailInfo> trailInfo) {
		this.multilayerInfo = compositeLayerInfo;
		this.layerInfo = layerInfo;
		this.trailInfo = trailInfo;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Deserializer implements JsonDeserializer<ClientAnimationDataReader> {
		static LayerInfo deserializeLayerInfo(JsonObject jsonObject) {
			return deserializeLayerInfo(jsonObject, null);
		}

		static LayerInfo deserializeLayerInfo(JsonObject jsonObject, Layer.LayerType defaultLayerType) {
			JointMaskEntry.Builder builder = JointMaskEntry.builder();
			Layer.Priority priority = jsonObject.has("priority") ? Layer.Priority.valueOf(GsonHelper.getAsString(jsonObject, "priority")) : null;
			Layer.LayerType layerType = jsonObject.has("layer") ? Layer.LayerType.valueOf(GsonHelper.getAsString(jsonObject, "layer")) : Layer.LayerType.BASE_LAYER;

			if (jsonObject.has("masks")) {
				builder.defaultMask(JointMaskEntry.ALL);
				JsonArray maskArray = jsonObject.get("masks").getAsJsonArray();

				maskArray.forEach(element -> {
					JsonObject jointMaskEntry = element.getAsJsonObject();
					String livingMotionName = GsonHelper.getAsString(jointMaskEntry, "livingmotion");
					String type = GsonHelper.getAsString(jointMaskEntry, "type");

					if (!type.contains(":")) {
						type = (new StringBuilder(EpicFightMod.MODID)).append(":").append(type).toString();
					}

					if (livingMotionName.equals("ALL")) {
						builder.defaultMask(JointMaskReloadListener.getJointMaskEntry(type));
					} else {
						builder.mask((LivingMotion) LivingMotion.ENUM_MANAGER.getOrThrow(livingMotionName), JointMaskReloadListener.getJointMaskEntry(type));
					}
				});
			}

			return new LayerInfo(builder.create(), priority, (defaultLayerType == null) ? layerType : defaultLayerType);
		}

		public ClientAnimationDataReader deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			LayerInfo layerInfo = null;
			LayerInfo multilayerInfo = null;

			if (jsonObject.has("multilayer")) {
				JsonObject multiplayerJson = jsonObject.get("multilayer").getAsJsonObject();
				layerInfo = deserializeLayerInfo(multiplayerJson.get("base").getAsJsonObject());
				multilayerInfo = deserializeLayerInfo(multiplayerJson.get("composite").getAsJsonObject(), Layer.LayerType.COMPOSITE_LAYER);
			} else {
				layerInfo = deserializeLayerInfo(jsonObject);
			}

			List<TrailInfo> trailInfos = Lists.newArrayList();

			if (jsonObject.has("trail_effects")) {
				JsonArray trailArray = jsonObject.get("trail_effects").getAsJsonArray();
				trailArray.forEach(element -> trailInfos.add(TrailInfo.deserialize(element)));
			}

			return new ClientAnimationDataReader(multilayerInfo, layerInfo, trailInfos);
		}
	}
}