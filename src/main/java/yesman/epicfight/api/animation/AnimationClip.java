package yesman.epicfight.api.animation;

import com.google.common.collect.Maps;

import java.util.Map;

public class AnimationClip {
	public static final AnimationClip EMPTY_CLIP = new AnimationClip();
	
	protected Map<String, TransformSheet> jointTransforms = Maps.newHashMap();
	protected float clipTime;
	
	public void addJointTransform(String jointName, TransformSheet sheet) {
		this.jointTransforms.put(jointName, sheet);
	}
	
	public boolean hasJointTransform(String jointName) {
		return this.jointTransforms.containsKey(jointName);
	}
	
	public TransformSheet getJointTransform(String jointName) {
		return this.jointTransforms.get(jointName);
	}
	
	public final Pose getPoseInTime(float time) {
		Pose pose = new Pose();

		if (time < 0.0F) {
			time = this.clipTime + time;
		}

		for (String jointName : this.jointTransforms.keySet()) {
			pose.putJointData(jointName, this.jointTransforms.get(jointName).getInterpolatedTransform(time));
		}
		
		return pose;
	}
	
	public Map<String, TransformSheet> getJointTransforms() {
		return this.jointTransforms;
	}
	public void setClipTime(float clipTime) {
		this.clipTime = clipTime;
	}
	
	public float getClipTime() {
		return this.clipTime;
	}
}