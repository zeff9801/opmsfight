package yesman.epicfight.api.model;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public class Armature {
	private final Map<Integer, Joint> jointById;
	private final Map<String, Joint> jointByName;
	private final Map<String, Integer> pathIndexMap;
	public final Joint rootJoint;
	private final int jointNumber;
	private final TransformSheet actionAnimationCoord = new TransformSheet();
	private Pose prevPose = new Pose();
	private Pose currentPose = new Pose();

	public Armature(int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		this.jointNumber = jointNumber;
		this.rootJoint = rootJoint;
		this.jointByName = jointMap;
		this.jointById = Maps.newHashMap();
		this.pathIndexMap = Maps.newHashMap();
		this.jointByName.values().forEach((joint) -> {
			this.jointById.put(joint.getId(), joint);
		});
	}

	public OpenMatrix4f[] getJointTransforms() {
		OpenMatrix4f[] jointMatrices = new OpenMatrix4f[this.jointNumber];
		this.jointToTransformMatrixArray(this.rootJoint, jointMatrices);
		return jointMatrices;
	}
	public Pose getPose(float partialTicks) {
		return Pose.interpolatePose(this.prevPose, this.currentPose, partialTicks);
	}

	public Pose getPrevPose() {
		return this.prevPose;
	}

	public Pose getCurrentPose() {
		return this.currentPose;
	}

	public void setPose(Pose pose) {
		this.prevPose = this.currentPose;
		this.currentPose = pose;
	}



	public OpenMatrix4f[] getAllPoseTransform(float partialTicks) {
		return this.getPoseAsTransformMatrix(this.getPose(partialTicks));
	}
	public OpenMatrix4f[] getPoseAsTransformMatrix(Pose pose) {
		OpenMatrix4f[] jointMatrices = new OpenMatrix4f[this.jointNumber];
		this.getPoseTransform(this.rootJoint, new OpenMatrix4f(), pose, jointMatrices);
		return jointMatrices;
	}

	public Joint searchJointById(int id) {
		return this.jointById.get(id);
	}

	public Joint searchJointByName(String name) {
		return this.jointByName.get(name);
	}
	
	public Collection<Joint> getJoints() {
		return this.jointByName.values();
	}
	
	public int searchPathIndex(String joint) {
		if (this.pathIndexMap.containsKey(joint)) {
			return this.pathIndexMap.get(joint);
		} else {
			String pathIndex = this.rootJoint.searchPath(new String(""), joint);
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
	
	public void initializeTransform() {
		this.rootJoint.initializeAnimationTransform();
	}

	public int getJointNumber() {
		return this.jointNumber;
	}

	public Joint getRootJoint() {
		return this.rootJoint;
	}

	private void jointToTransformMatrixArray(Joint joint, OpenMatrix4f[] jointMatrices) {
		OpenMatrix4f result = OpenMatrix4f.mul(joint.getAnimatedTransform(), joint.getInversedModelTransform(), null);
		jointMatrices[joint.getId()] = result;
		
		for (Joint childJoint : joint.getSubJoints()) {
			this.jointToTransformMatrixArray(childJoint, jointMatrices);
		}
	}
	private void getPoseTransform(Joint joint, OpenMatrix4f parentTransform, Pose pose, OpenMatrix4f[] jointMatrices) {
		OpenMatrix4f result = pose.getOrDefaultTransform(joint.getName()).getAnimationBindedMatrix(joint, parentTransform);
		jointMatrices[joint.getId()] = result;

		for (Joint joints : joint.getSubJoints()) {
			this.getPoseTransform(joints, result, pose, jointMatrices);
		}
	}

	public OpenMatrix4f getBindedTransformForCurrentPose(Joint joint) {
		return this.getBindedTransformByJointIndex(this.getCurrentPose(), this.searchPathIndex(joint.getName()));
	}

	public OpenMatrix4f getBindedTransformFor(Pose pose, Joint joint) {
		return this.getBindedTransformByJointIndex(pose, this.searchPathIndex(joint.getName()));
	}

	/** Get binded position of joint **/
	public OpenMatrix4f getBindedTransformByJointIndex(Pose pose, int pathIndex) {
		this.initializeTransform();
		return getBindedJointTransformByIndexInternal(pose, this.rootJoint, new OpenMatrix4f(), pathIndex);
	}

	private OpenMatrix4f getBindedJointTransformByIndexInternal(Pose pose, Joint joint, OpenMatrix4f parentTransform, int pathIndex) {
		JointTransform jt = pose.getOrDefaultTransform(joint.getName());
		OpenMatrix4f result = jt.getAnimationBindedMatrix(joint, parentTransform);
		int nextIndex = pathIndex % 10;
		return nextIndex > 0 ? this.getBindedJointTransformByIndexInternal(pose, joint.getSubJoints().get(nextIndex - 1), result, pathIndex / 10) : result;
	}
}