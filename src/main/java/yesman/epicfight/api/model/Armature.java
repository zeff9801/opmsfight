
package yesman.epicfight.api.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.main.EpicFightMod;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Armature {
	private final String name;
	private final Int2ObjectMap<Joint> jointById;
	private final Map<String, Joint> jointByName;
	private final Object2IntMap<String> pathIndexMap;
	private final int jointNumber;
	public final Joint rootJoint;
	private final TransformSheet actionAnimationCoord = new TransformSheet();

	public Armature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		this.name = name;
		this.jointNumber = jointNumber;
		this.rootJoint = rootJoint;
		this.jointByName = jointMap;
		this.jointById = new Int2ObjectOpenHashMap<>();
		this.pathIndexMap = new Object2IntOpenHashMap<>();
		this.jointByName.values().forEach((joint) -> {
			this.jointById.put(joint.getId(), joint);
		});
	}

	protected Joint getOrLogException(Map<String, Joint> jointMap, String name) {
		if (!jointMap.containsKey(name)) {
			EpicFightMod.LOGGER.debug("Cannot find the joint named " + name + " in " + this.getClass().getCanonicalName());

			return Joint.EMPTY;
		}

		return jointMap.get(name);
	}

	public OpenMatrix4f[] getPoseAsTransformMatrix(Pose pose) {
		OpenMatrix4f[] jointMatrices = new OpenMatrix4f[this.jointNumber];
		this.getPoseTransform(this.rootJoint, new OpenMatrix4f(), pose, jointMatrices);
		return jointMatrices;
	}

	private void getPoseTransform(Joint joint, OpenMatrix4f parentTransform, Pose pose, OpenMatrix4f[] jointMatrices) {
		OpenMatrix4f result = pose.getOrDefaultTransform(joint.getName()).getAnimationBindedMatrix(joint, parentTransform);
		jointMatrices[joint.getId()] = result;

		for (Joint joints : joint.getSubJoints()) {
			this.getPoseTransform(joints, result, pose, jointMatrices);
		}
	}

	public OpenMatrix4f getBindedTransformFor(Pose pose, Joint joint) {
		return this.getBindedTransformByJointIndex(pose, this.searchPathIndex(joint.getName()));
	}

	/** Get binded position of joint **/
	public OpenMatrix4f getBindedTransformByJointIndex(Pose pose, int pathIndex) {
		return getBindedJointTransformByIndexInternal(pose, this.rootJoint, new OpenMatrix4f(), pathIndex);
	}

	private OpenMatrix4f getBindedJointTransformByIndexInternal(Pose pose, Joint joint, OpenMatrix4f parentTransform, int pathIndex) {
		JointTransform jt = pose.getOrDefaultTransform(joint.getName());
		OpenMatrix4f result = jt.getAnimationBindedMatrix(joint, parentTransform);
		int nextIndex = pathIndex % 10;
		return nextIndex > 0 ? this.getBindedJointTransformByIndexInternal(pose, joint.getSubJoints().get(nextIndex - 1), result, pathIndex / 10) : result;
	}

	public Joint searchJointById(int id) {
		return this.jointById.get(id);
	}

	public Joint searchJointByName(String name) {
		return this.jointByName.get(name);
	}

	public int searchPathIndex(String joint) {
		if (this.pathIndexMap.containsKey(joint)) {
			return this.pathIndexMap.getInt(joint);
		} else {
			String pathIndex = this.rootJoint.searchPath("", joint);
			int pathIndex2Int = 0;

			if (pathIndex == null) {
				throw new IllegalArgumentException("failed to get joint path index for " + joint);
			} else {
				pathIndex2Int = (pathIndex.length() == 0) ? -1 : Integer.parseInt(pathIndex);
				this.pathIndexMap.put(joint, pathIndex2Int);
			}

			return pathIndex2Int;
		}
	}

	public TransformSheet getActionAnimationCoord() {
		return this.actionAnimationCoord;
	}

	public int getJointNumber() {
		return this.jointNumber;
	}

	public Joint getRootJoint() {
		return this.rootJoint;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public Armature deepCopy() {
		Map<String, Joint> oldToNewJoint = Maps.newHashMap();
		oldToNewJoint.put("empty", Joint.EMPTY);

		Joint newRoot = this.copyHierarchy(this.rootJoint, oldToNewJoint);
		newRoot.initOriginTransform(new OpenMatrix4f());
		Armature newArmature = null;

		//Uses reflection to keep the type of copied armature
		try {
			Constructor<? extends Armature> constructor = this.getClass().getConstructor(String.class, int.class, Joint.class, Map.class);
			newArmature = constructor.newInstance(this.name, this.jointNumber, newRoot, oldToNewJoint);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("Armature copy failed! " + e);
		}

		return newArmature;
	}

	private Joint copyHierarchy(Joint joint, Map<String, Joint> oldToNewJoint) {
		if (joint == Joint.EMPTY) {
			return Joint.EMPTY;
		}

		Joint newJoint = new Joint(joint.getName(), joint.getId(), joint.getLocalTrasnform());
		oldToNewJoint.put(joint.getName(), newJoint);

		for (Joint subJoint : joint.getSubJoints()) {
			newJoint.addSubJoint(this.copyHierarchy(subJoint, oldToNewJoint));
		}

		return newJoint;
	}

	public JsonObject toJsonObject() {
		JsonObject root = new JsonObject();
		JsonObject armature = new JsonObject();

		JsonArray jointNamesArray = new JsonArray();
		JsonArray jointHierarchy = new JsonArray();

		this.jointById.int2ObjectEntrySet().stream().sorted((entry1, entry2) -> Integer.compare(entry1.getIntKey(), entry2.getIntKey())).forEach((entry) -> jointNamesArray.add(entry.getValue().getName()));
		armature.add("joints", jointNamesArray);
		armature.add("hierarchy", jointHierarchy);

		exportJoint(jointHierarchy, this.rootJoint, true);

		root.add("armature", armature);

		return root;
	}

	private static void exportJoint(JsonArray parent, Joint joint, boolean root) {
		JsonObject jointJson = new JsonObject();
		jointJson.addProperty("name", joint.getName());

		JsonArray transformMatrix = new JsonArray();
		OpenMatrix4f localMatrixInBlender = new OpenMatrix4f(joint.getLocalTrasnform());

		if (root) {
			localMatrixInBlender.mulFront(OpenMatrix4f.invert(JsonModelLoader.BLENDER_TO_MINECRAFT_COORD, null));
		}

		localMatrixInBlender.transpose();
		localMatrixInBlender.toList().forEach(transformMatrix::add);
		jointJson.add("transform", transformMatrix);
		parent.add(jointJson);

		if (!joint.getSubJoints().isEmpty()) {
			JsonArray children = new JsonArray();
			jointJson.add("children", children);
			joint.getSubJoints().forEach((joint$2) -> exportJoint(children, joint$2, false));
		}
	}
}
