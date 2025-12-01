package yesman.epicfight.api.animation;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class Pose {
	public static final Pose EMPTY_POSE = new Pose();
	private final Map<String, JointTransform> jointTransformData = Maps.newHashMap();
	private static final Set<String> MERGED_SET_HOLDER = new HashSet<>();

	public void putJointData(String name, JointTransform transform) {
		this.jointTransformData.put(name, transform);
	}

	public void putJointData(Pose pose) {
		this.jointTransformData.putAll(pose.jointTransformData);
	}

	public void clear() {
		this.jointTransformData.clear();
	}

	public Map<String, JointTransform> getJointTransformData() {
		return this.jointTransformData;
	}
	public void disableJoint(Predicate<? super Map.Entry<String, JointTransform>> predicate) {
		this.jointTransformData.entrySet().removeIf(predicate);
	}

	public void disableAllJoints() {
		this.jointTransformData.clear();
	}

	public boolean hasTransform(String jointName) {
		return this.jointTransformData.containsKey(jointName);
	}
	public JointTransform orElseEmpty(String jointName) {
		return this.jointTransformData.getOrDefault(jointName, JointTransform.empty());
	}

	public static Pose interpolatePose(Pose pose1, Pose pose2, float pregression) {
		return interpolatePose(pose1, pose2, pregression, new Pose(), MERGED_SET_HOLDER);
	}

	public static Pose interpolatePose(Pose pose1, Pose pose2, float pregression, Pose dest, Set<String> mergedSet) {
		dest.clear();
		mergedSet.clear();
		mergedSet.addAll(pose1.jointTransformData.keySet());
		mergedSet.addAll(pose2.jointTransformData.keySet());

		for (String jointName : mergedSet) {
			dest.putJointData(jointName, JointTransform.interpolate(pose1.orElseEmpty(jointName), pose2.orElseEmpty(jointName), pregression));
		}

		return dest;
	}

	public void forEachEnabledTransforms(BiConsumer<String, JointTransform> task) {
		this.jointTransformData.forEach(task);
	}

	public void load(Pose pose, LoadOperation operation) {
		switch (operation) {
			case SET -> {
				this.disableAllJoints();
				pose.forEachEnabledTransforms(this::putJointData);
			}
			case OVERWRITE -> {
				pose.forEachEnabledTransforms(this::putJointData);
			}
			case APPEND_ABSENT -> {
				pose.forEachEnabledTransforms((name, transform) -> {
					if (!this.hasTransform(name)) {
						this.putJointData(name, transform);
					}
				});
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Pose: ");

		for (Map.Entry<String, JointTransform> entry : this.jointTransformData.entrySet()) {
			sb.append(String.format("%s{%s, %s}, ", entry.getKey(), entry.getValue().translation().toString(), entry.getValue().rotation().toString())).append("\n");
		}

		return sb.toString();
	}
	public enum LoadOperation {
		SET, OVERWRITE, APPEND_ABSENT
	}
}
