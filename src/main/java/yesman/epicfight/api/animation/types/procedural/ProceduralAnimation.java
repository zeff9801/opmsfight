package yesman.epicfight.api.animation.types.procedural;

import com.google.common.collect.Lists;
import com.joml.Quaternionf;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.FABRIK;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;

import java.util.Map;

public interface ProceduralAnimation {
	default void setIKInfo(IKInfo[] ikInfos, Map<String, TransformSheet> src, Map<String, TransformSheet> dest, Armature armature, boolean correctY, boolean correctZ) {
		for (IKInfo ikInfo : ikInfos) {
			ikInfo.pathToEndJoint = Lists.newArrayList();
			Joint start = armature.searchJointByName(ikInfo.startJoint.getName());
			int pathToEnd = Integer.parseInt(start.searchPath("", ikInfo.endJoint.getName()));
			ikInfo.pathToEndJoint.add(start.getName());
			
			while (pathToEnd > 0) {
				start = start.getSubJoints().get(pathToEnd % 10 - 1);
				pathToEnd /= 10;
				ikInfo.pathToEndJoint.add(start.getName());
			}
			
			Keyframe[] keyframes = src.get(ikInfo.endJoint.getName()).getKeyframes();
			Keyframe[] bindedposKeyframes = new Keyframe[keyframes.length];
			int keyframeLength = src.get(ikInfo.endJoint.getName()).getKeyframes().length;
			
			for (int i = 0; i < keyframeLength; i++) {
				Keyframe kf = keyframes[i];
				Pose pose = new Pose();
				
				for (String jointName : src.keySet()) {
					pose.putJointData(jointName, src.get(jointName).getInterpolatedTransform(kf.time()));
				}
				
				OpenMatrix4f bindedTransform = armature.getBindedTransformFor(pose, ikInfo.endJoint);
				JointTransform bindedJointTransform = JointTransform.fromMatrixNoScale(bindedTransform);
				bindedposKeyframes[i] = new Keyframe(kf);
				JointTransform tipTransform = bindedposKeyframes[i].transform();
				tipTransform.copyFrom(bindedJointTransform);
				
				if (correctY || correctZ) {
					JointTransform rootTransform = src.get("Root").getInterpolatedTransform(kf.time());
					Vec3f rootPos = rootTransform.translation();
					float yCorrection = correctY ? -rootPos.z : 0.0F;
					float zCorrection = correctZ ? rootPos.y : 0.0F;
					tipTransform.translation().add(0.0F, yCorrection, zCorrection);
				}
			}
			
			TransformSheet tipAnimation = new TransformSheet(bindedposKeyframes);
			dest.put(ikInfo.endJoint.getName(), tipAnimation);
			
			if (ikInfo.clipAnimation) {
				TransformSheet part = tipAnimation.copy(ikInfo.startFrame, ikInfo.endFrame);
				Keyframe[] partKeyframes = part.getKeyframes();
				ikInfo.startpos = partKeyframes[0].transform().translation();
				ikInfo.endpos = partKeyframes[partKeyframes.length - 1].transform().translation();
			} else {
				ikInfo.startpos = tipAnimation.getKeyframes()[0].transform().translation();
				ikInfo.endpos = ikInfo.startpos;
			}
			
			ikInfo.startToEnd = Vec3f.sub(ikInfo.endpos, ikInfo.startpos, null).multiply(-1.0F, 1.0F, -1.0F);
		}
	}
	
	default TransformSheet getFirstPart(TransformSheet transformSheet) {
		TransformSheet part = transformSheet.copy(0, 2);
		Keyframe[] keyframes = part.getKeyframes();
		keyframes[1].transform().copyFrom(keyframes[0].transform());
		return part;
	}
	
	default TransformSheet clipAnimation(TransformSheet transformSheet, IKInfo ikInfo) {
		if (ikInfo.clipAnimation) {
			return transformSheet.copy(ikInfo.startFrame, ikInfo.endFrame);
		} else {
			return this.getFirstPart(transformSheet);
		}
	}
	
	default void applyFabrikToJoint(Vec3f recalculatedPosition, Pose pose, Armature armature, Joint startJoint, Joint endJoint, Quaternionf tipRotation) {
		FABRIK fabrik = new FABRIK(pose, armature, startJoint, endJoint);
    	fabrik.run(recalculatedPosition, 10);
    	OpenMatrix4f tipRotationMatrix = OpenMatrix4f.fromQuaternionf(tipRotation);
    	OpenMatrix4f animRotation = armature.getBindedTransformFor(pose, endJoint).removeTranslation();
    	OpenMatrix4f animToTipRotation = OpenMatrix4f.mul(OpenMatrix4f.invert(animRotation, null), tipRotationMatrix, null);
    	pose.getOrDefaultTransform(endJoint.getName()).overwriteRotation(JointTransform.fromMatrixNoScale(animToTipRotation));
	}
	
	default void startPartAnimation(IKInfo ikInfo, TipPointAnimation tipAnim, TransformSheet partAnimation, Vec3f targetpos) {
		Vec3f footpos = tipAnim.getTipPosition(1.0F);
		Vec3f worldStartToEnd = targetpos.copy().sub(footpos);
		partAnimation.correctAnimationByNewPosition(ikInfo.startpos, ikInfo.startToEnd, footpos, worldStartToEnd);
		tipAnim.start(targetpos, partAnimation, 1.0F);
	}
	
	default void startSimple(IKInfo ikInfo, TipPointAnimation tipAnim) {
		tipAnim.start(new Vec3f(0.0F, 0.0F, 0.0F), tipAnim.getAnimation(), 1.0F);
	}
}