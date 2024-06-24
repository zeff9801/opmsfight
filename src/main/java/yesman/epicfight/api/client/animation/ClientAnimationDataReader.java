package yesman.epicfight.api.client.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.property.GsonHelper;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.animation.property.TrailInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientAnimationDataReader {
	public static final ClientAnimationDataReader.Deserializer DESERIALIZER = new ClientAnimationDataReader.Deserializer();
	private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ClientAnimationDataReader.class, DESERIALIZER).create();
	private static final TypeToken<ClientAnimationDataReader> TYPE = new TypeToken<ClientAnimationDataReader>() {};
	private final LayerInfo layerInfo;
	private final LayerInfo multilayerInfo;
	private final List<TrailInfo> trailInfo;

	public static void readAndApply(StaticAnimation animation, IResourceManager resourceManager, IResource iresource) {

		InputStream inputstream = iresource.getInputStream();
		Reader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
		ClientAnimationDataReader propertySetter = GsonHelper.fromJson(GSON, reader, TYPE);

		if (propertySetter.layerInfo != null) {
			if (propertySetter.layerInfo.jointMaskEntry.isValid()) {
				animation.addProperty(ClientAnimationProperties.JOINT_MASK, propertySetter.layerInfo.jointMaskEntry);
			}

			animation.addProperty(ClientAnimationProperties.LAYER_TYPE, propertySetter.layerInfo.layerType);
			animation.addProperty(ClientAnimationProperties.PRIORITY, propertySetter.layerInfo.priority);
		}
		}

	private ClientAnimationDataReader(LayerInfo compositeLayerInfo, LayerInfo layerInfo, List<TrailInfo> trailInfo) {
		this.multilayerInfo = compositeLayerInfo;
		this.layerInfo = layerInfo;
		this.trailInfo = trailInfo;
	}

	static class Deserializer implements JsonDeserializer<ClientAnimationDataReader> {
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

					if (livingMotionName.equals("ALL")) {
						builder.defaultMask(JointMaskReloadListener.getJointMaskEntry(GsonHelper.getAsString(jointMaskEntry, "type")));
					} else {
						builder.mask((LivingMotion) LivingMotion.ENUM_MANAGER.get(livingMotionName), JointMaskReloadListener.getJointMaskEntry(GsonHelper.getAsString(jointMaskEntry, "type")));
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
	private static final Map<String, List<JointMask>> JOINT_MASKS = Maps.newHashMap();

	public static void registerJointMask(String name, List<JointMask> jointMask) {
		JOINT_MASKS.put(name, jointMask);
	}

}