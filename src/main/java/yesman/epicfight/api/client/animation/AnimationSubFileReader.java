package yesman.epicfight.api.client.animation;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.IResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.property.*;
import yesman.epicfight.main.EpicFightMod;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AnimationSubFileReader {

	public static void readAndApply(StaticAnimation animation, IResource iresource, SubFileType<?> subFileType) {

		InputStream inputstream = iresource.getInputStream();
        try {
			subFileType.apply(inputstream, animation);
		} catch (JsonParseException e) {
            EpicFightMod.LOGGER.warn("Can't read sub file {} for {}", subFileType.directory, animation);
			e.printStackTrace();
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class SubFileType<T> {
		private final String directory;
		private final AnimationSubFileDeserializer<T> deserializer;
		
		private SubFileType(String directory,  AnimationSubFileDeserializer<T> deserializer) {
			this.directory = directory;
			this.deserializer = deserializer;
		}
		
		// Deserialize from input stream
		public void apply(InputStream inputstream, StaticAnimation animation) {
			Reader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			T deserialized = this.deserializer.deserialize(animation, Streams.parse(jsonReader));
			this.applySubFileInfo(deserialized, animation);
		}
		
		// Deserialize from json object
		public void apply(JsonElement jsonElement, StaticAnimation animation) {
			T deserialized = this.deserializer.deserialize(animation, jsonElement);
			this.applySubFileInfo(deserialized, animation);
		}
		
		protected abstract void applySubFileInfo(T deserialized, StaticAnimation animation);
		
		public String getDirectory() {
			return this.directory;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private record ClientProperty(LayerInfo layerInfo, LayerInfo multilayerInfo, List<TrailInfo> trailInfo) {
	}

	
	@OnlyIn(Dist.CLIENT)
	private static class ClientAnimationPropertyDeserializer implements AnimationSubFileDeserializer<ClientProperty> {
		private static LayerInfo deserializeLayerInfo(JsonObject jsonObject) {
			return deserializeLayerInfo(jsonObject, null);
		}
		
		private static LayerInfo deserializeLayerInfo(JsonObject jsonObject, @Nullable Layer.LayerType defaultLayerType) {
			JointMaskEntry.Builder builder = JointMaskEntry.builder();
			Layer.Priority priority = jsonObject.has("priority") ? Layer.Priority.valueOf(GsonHelper.getAsString(jsonObject, "priority")) : null;
			Layer.LayerType layerType = jsonObject.has("layer") ? Layer.LayerType.valueOf(GsonHelper.getAsString(jsonObject, "layer")) : Layer.LayerType.BASE_LAYER;
			
			if (jsonObject.has("masks")) {
				JsonArray maskArray = jsonObject.get("masks").getAsJsonArray();

				if (!(maskArray.size() == 0)) {
					builder.defaultMask(JointMaskReloadListener.getNoneMask());
					
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
			}
			
			return new LayerInfo(builder.create(), priority, (defaultLayerType == null) ? layerType : defaultLayerType);
		}
		
		@Override
		public ClientProperty deserialize(StaticAnimation animation, JsonElement json) throws JsonParseException {
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
			
			return new ClientProperty(layerInfo, multilayerInfo, trailInfos);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static record PovSettings(
		@Nullable TransformSheet cameraTransform,
		Map<String, Boolean> visibilities,
		RootTransformation rootTransformation,
		@Nullable ViewLimit viewLimit,
		boolean visibilityOthers,
		boolean hasUniqueAnimation,
		boolean syncFrame
	) {
		@OnlyIn(Dist.CLIENT)
		public enum RootTransformation {
			CAMERA, WORLD
		}
		
		@OnlyIn(Dist.CLIENT)
		public record ViewLimit(float xRotMin, float xRotMax, float yRotMin, float yRotMax) {
		}

	}
	
	@OnlyIn(Dist.CLIENT)
	public interface AnimationSubFileDeserializer<T> {
		public T deserialize(StaticAnimation animation, JsonElement json) throws JsonParseException;
	}
}