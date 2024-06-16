package yesman.epicfight.api.model;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.model.ClientModel;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.main.EpicFightMod;

public class JsonModelLoader {

	public static final OpenMatrix4f BLENDER_TO_MINECRAFT_COORD = OpenMatrix4f.createRotatorDeg(-90.0F, Vec3f.X_AXIS);

	private JsonObject rootJson;
	private IResourceManager resourceManager;
	private ResourceLocation resourceLocation;

	public JsonModelLoader(IResourceManager resourceManager, ResourceLocation resourceLocation) throws IllegalStateException {
		JsonReader jsonReader = null;
		this.resourceManager = resourceManager;
		this.resourceLocation = resourceLocation;

		try {
			try {
				IResource resource = resourceManager.getResource(resourceLocation);
				jsonReader = new JsonReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
				jsonReader.setLenient(true);
				this.rootJson = Streams.parse(jsonReader).getAsJsonObject();
			} catch (NoSuchElementException e) {
				// In this case, reads the animation data from mod.jar (Especially in a server)
				Class<?> modClass = ModList.get().getModObjectById(resourceLocation.getNamespace()).get().getClass();
				InputStream inputStream = modClass.getResourceAsStream("/assets/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());

				if (inputStream == null) {
					throw new NoSuchElementException("Can't find specified file in mod resource " + resourceLocation);
				}

				BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				Reader reader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
				jsonReader = new JsonReader(reader);
				jsonReader.setLenient(true);
				this.rootJson = Streams.parse(jsonReader).getAsJsonObject();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Can't read " + resourceLocation.toString() + " because of " + e);
		} finally {
			if (jsonReader != null) {
				try {
					jsonReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@OnlyIn(Dist.CLIENT)
	public JsonModelLoader(InputStream inputstream, ResourceLocation resourceLocation) throws IOException {
		JsonReader jsonReader = null;
		this.resourceManager = Minecraft.getInstance().getResourceManager();
		this.resourceLocation = resourceLocation;

		jsonReader = new JsonReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
		jsonReader.setLenient(true);
		this.rootJson = Streams.parse(jsonReader).getAsJsonObject();
		jsonReader.close();
	}

	@OnlyIn(Dist.CLIENT)
	public JsonModelLoader(JsonObject rootJson, ResourceLocation rl) throws IOException {
		this.resourceManager = Minecraft.getInstance().getResourceManager();
		this.rootJson = rootJson;
		this.resourceLocation = rl;
	}

	public boolean isValidSource() {
		return this.rootJson != null;
	}

	@OnlyIn(Dist.CLIENT)
	public ClientModel.RenderProperties getRenderProperties() {
		JsonObject properties = this.rootJson.getAsJsonObject("render_properties");

		if (properties != null) {
			return ClientModel.RenderProperties.builder()
					.transparency(properties.has("transparent") && properties.get("transparent").getAsBoolean())
				.build();
		} else {
			return ClientModel.RenderProperties.builder().build();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getParent() {
		return this.rootJson.has("parent") ? new ResourceLocation(this.rootJson.get("parent").getAsString()) : null;
	}

	@OnlyIn(Dist.CLIENT)
	public Mesh getMesh() {
		JsonObject obj = this.rootJson.getAsJsonObject("vertices");
		JsonObject positions = obj.getAsJsonObject("positions");
		JsonObject normals = obj.getAsJsonObject("normals");
		JsonObject uvs = obj.getAsJsonObject("uvs");
		JsonObject parts = obj.getAsJsonObject("parts");
		JsonObject indices = obj.getAsJsonObject("indices");
		JsonObject vdincies = obj.getAsJsonObject("vindices");
		JsonObject weights = obj.getAsJsonObject("weights");
		JsonObject drawingIndices = obj.getAsJsonObject("indices");
		JsonObject vcounts = obj.getAsJsonObject("vcounts");

		float[] positionArray = ParseUtil.toFloatArray(positions.get("array").getAsJsonArray());

		for (int i = 0; i < positionArray.length / 3; i++) {
			int k = i * 3;
			Vec4f posVector = new Vec4f(positionArray[k], positionArray[k+1], positionArray[k+2], 1.0F);
			OpenMatrix4f.transform(BLENDER_TO_MINECRAFT_COORD, posVector, posVector);
			positionArray[k] = posVector.x;
			positionArray[k+1] = posVector.y;
			positionArray[k+2] = posVector.z;
		}

		float[] normalArray = ParseUtil.toFloatArray(normals.get("array").getAsJsonArray());

		for (int i = 0; i < normalArray.length / 3; i++) {
			int k = i * 3;
			Vec4f normVector = new Vec4f(normalArray[k], normalArray[k+1], normalArray[k+2], 1.0F);
			OpenMatrix4f.transform(BLENDER_TO_MINECRAFT_COORD, normVector, normVector);
			normalArray[k] = normVector.x;
			normalArray[k+1] = normVector.y;
			normalArray[k+2] = normVector.z;
		}

		int[] drawingIndexArray = ParseUtil.toIntArray(drawingIndices.get("array").getAsJsonArray());


		float[] uvArray = ParseUtil.toFloatArray(uvs.get("array").getAsJsonArray());
		int[] animationIndexArray = ParseUtil.toIntArray(vdincies.get("array").getAsJsonArray());
		float[] weightArray = ParseUtil.toFloatArray(weights.get("array").getAsJsonArray());
		int[] vcountArray = ParseUtil.toIntArray(vcounts.get("array").getAsJsonArray());


		return new Mesh(positionArray, normalArray, uvArray, animationIndexArray, weightArray, drawingIndexArray, vcountArray);
	}

	public Armature getArmature() {
		JsonObject obj = this.rootJson.getAsJsonObject("armature");
		JsonObject hierarchy = obj.get("hierarchy").getAsJsonArray().get(0).getAsJsonObject();
		JsonArray nameAsVertexGroups = obj.getAsJsonArray("joints");
		Map<String, Joint> jointMap = Maps.newHashMap();
		Joint joint = this.getJoint(hierarchy, nameAsVertexGroups, jointMap, true);
		joint.initOriginTransform(new OpenMatrix4f());
		return new Armature(jointMap.size(), joint, jointMap);
	}

	public Joint getJoint(JsonObject object, JsonArray nameAsVertexGroups, Map<String, Joint> jointMap, boolean start) {
		float[] floatArray = ParseUtil.toFloatArray(object.get("transform").getAsJsonArray());
		OpenMatrix4f localMatrix = new OpenMatrix4f().load(FloatBuffer.wrap(floatArray));
		localMatrix.transpose();
		if (start) {
			localMatrix.mulFront(BLENDER_TO_MINECRAFT_COORD);
		}

		String name = object.get("name").getAsString();
		int index = -1;

		for (int i = 0; i < nameAsVertexGroups.size(); i++) {
			if (name.equals(nameAsVertexGroups.get(i).getAsString())) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			throw new IllegalStateException("[ModelParsingError]: Joint name " + name + " not exist!");
		}

		Joint joint = new Joint(name, index, localMatrix);
		jointMap.put(name, joint);

		for (JsonElement children : object.get("children").getAsJsonArray()) {
			joint.addSubJoint(this.getJoint(children.getAsJsonObject(), nameAsVertexGroups, jointMap, false));
		}

		return joint;
	}

	public void loadStaticAnimation(StaticAnimation animation) {
		if (this.rootJson == null) {
			throw new IllegalStateException("[ModelParsingError]Can't find animation path: " + animation);
		}

		JsonArray array = this.rootJson.get("animation").getAsJsonArray();
		boolean action = animation instanceof ActionAnimation;
		boolean attack = animation instanceof AttackAnimation;
		boolean noTransformData = !action && !attack && FMLEnvironment.dist == Dist.DEDICATED_SERVER;
		boolean root = true;
		Armature armature = animation.getModel().getArmature();

		Set<String> allowedJoints = Sets.newLinkedHashSet();

		if (attack) {
			for (Phase phase : ((AttackAnimation)animation).phases) {
				Joint joint = armature.getRootJoint();
				int pathIndex = armature.searchPathIndex(phase.getColliderJointName());

				while (joint != null) {
					allowedJoints.add(joint.getName());
					int nextJoint = pathIndex % 10;

					if (nextJoint > 0) {
						pathIndex /= 10;
						joint = joint.getSubJoints().get(nextJoint - 1);
					} else {
						joint = null;
					}
				}
			}
		} else if (action) {
			allowedJoints.add("Root");
		}

		for (JsonElement element : array) {
			JsonObject keyObject = element.getAsJsonObject();
			String name = keyObject.get("name").getAsString();

			if (attack && FMLEnvironment.dist == Dist.DEDICATED_SERVER && !allowedJoints.contains(name)) {
				if (name.equals("Coord")) {
					root = false;
				}

				continue;
			}

			Joint joint = armature.searchJointByName(name);

			if (joint == null) {
				if (name.equals("Coord") && action) {
					JsonArray timeArray = keyObject.getAsJsonArray("time");
					JsonArray transformArray = keyObject.getAsJsonArray("transform");
					int timeNum = timeArray.size();
					int matrixNum = transformArray.size();
					float[] times = new float[timeNum];
					float[] transforms = new float[matrixNum * 16];

					for (int i = 0; i < timeNum; i++) {
						times[i] = timeArray.get(i).getAsFloat();
					}

					for (int i = 0; i < matrixNum; i++) {
						JsonArray matrixJson = transformArray.get(i).getAsJsonArray();

						for (int j = 0; j < 16; j++) {
							transforms[i * 16 + j] = matrixJson.get(j).getAsFloat();
						}
					}

					TransformSheet sheet = getTransformSheet(times, transforms, new OpenMatrix4f(), true);
					((ActionAnimation)animation).addProperty(AnimationProperty.MoveCoordFunctions.COORD, sheet);
					root = false;
					continue;
				} else {
					EpicFightMod.LOGGER.warn("[EpicFightMod] Can't find the joint " + name + " in the animation file, " + animation);
					continue;
				}
			}

			JsonArray timeArray = keyObject.getAsJsonArray("time");
			JsonArray transformArray = keyObject.getAsJsonArray("transform");
			int timeNum = timeArray.size();
			int matrixNum = transformArray.size();
			float[] times = new float[timeNum];
			float[] transforms = new float[matrixNum * 16];

			for (int i = 0; i < timeNum; i++) {
				times[i] = timeArray.get(i).getAsFloat();
			}

			for (int i = 0; i < matrixNum; i++) {
				JsonArray matrixJson = transformArray.get(i).getAsJsonArray();

				for (int j = 0; j < 16; j++) {
					transforms[i * 16 + j] = matrixJson.get(j).getAsFloat();
				}
			}

			TransformSheet sheet = getTransformSheet(times, transforms, OpenMatrix4f.invert(joint.getLocalTrasnform(), null), root);

			if (!noTransformData) {
				animation.addSheet(name, sheet);
			}

			animation.setTotalTime(times[times.length - 1]);
			root = false;
		}
	}

	public void loadStaticAnimationBothSide(StaticAnimation animation) {
		JsonArray array = this.rootJson.get("animation").getAsJsonArray();
		boolean root = true;
		Armature armature = animation.getModel().getArmature();

		for (JsonElement element : array) {
			JsonObject keyObject = element.getAsJsonObject();
			String name = keyObject.get("name").getAsString();
			Joint joint = armature.searchJointByName(name);

			if (joint == null) {
				continue;
            }

			JsonArray timeArray = keyObject.getAsJsonArray("time");
			JsonArray transformArray = keyObject.getAsJsonArray("transform");
			int timeNum = timeArray.size();
			int matrixNum = transformArray.size();
			float[] times = new float[timeNum];
			float[] transforms = new float[matrixNum * 16];

			for (int i = 0; i < timeNum; i++) {
				times[i] = timeArray.get(i).getAsFloat();
			}

			for (int i = 0; i < matrixNum; i++) {
				JsonArray matrixJson = transformArray.get(i).getAsJsonArray();

				for (int j = 0; j < 16; j++) {
					transforms[i * 16 + j] = matrixJson.get(j).getAsFloat();
				}
			}

			TransformSheet sheet = getTransformSheet(times, transforms, OpenMatrix4f.invert(joint.getLocalTrasnform(), null), root);
			animation.addSheet(name, sheet);
			animation.setTotalTime(times[times.length - 1]);
			root = false;
		}
	}

	private static TransformSheet getTransformSheet(float[] times, float[] trasnformMatrix, OpenMatrix4f invLocalTransform, boolean correct) {
		List<Keyframe> keyframeList = Lists.newArrayList();

		for (int i = 0; i < times.length; i++) {
			float timeStamp = times[i];

			if (timeStamp < 0) {
				continue;
			}

			float[] matrixElements = new float[16];

            System.arraycopy(trasnformMatrix, i * 16, matrixElements, 0, 16);

			OpenMatrix4f matrix = OpenMatrix4f.load(null, FloatBuffer.wrap(matrixElements));
			matrix.transpose();

			if (correct) {
				matrix.mulFront(BLENDER_TO_MINECRAFT_COORD);
			}

			matrix.mulFront(invLocalTransform);

			JointTransform transform = new JointTransform(matrix.toTranslationVector(), matrix.toQuaternion(), matrix.toScaleVector());
			keyframeList.add(new Keyframe(timeStamp, transform));
		}

        return new TransformSheet(keyframeList);
	}
}