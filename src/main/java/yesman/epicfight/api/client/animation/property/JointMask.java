package yesman.epicfight.api.client.animation.property;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class JointMask {
	private static final OpenMatrix4f MATRIX_HOLDER_1 = new OpenMatrix4f(); 
	private static final OpenMatrix4f MATRIX_HOLDER_2 = new OpenMatrix4f(); 
	private static final OpenMatrix4f MATRIX_HOLDER_3 = new OpenMatrix4f(); 
	private static final OpenMatrix4f MATRIX_HOLDER_4 = new OpenMatrix4f();
	private static final OpenMatrix4f MATRIX_HOLDER_5 = new OpenMatrix4f(); 
	private static final OpenMatrix4f MATRIX_HOLDER_6 = new OpenMatrix4f();
	private static final OpenMatrix4f MATRIX_HOLDER_7 = new OpenMatrix4f(); 
	private static final Vec3f VEC_HOLDER = new Vec3f(); 
	private static final JointTransform JT_HOLDER_1 = JointTransform.empty();
	private static final JointTransform JT_HOLDER_2 = JointTransform.empty();

	@OnlyIn(Dist.CLIENT)
	@FunctionalInterface
	public interface BindModifier {
		public void modify(LivingEntityPatch<?> entitypatch, Pose baseLayerPose, Pose resultPose, LivingMotion livingMotion, JointMaskEntry wholeEntry, Layer.Priority priority, Joint joint, Map<Layer.Priority, Pair<DynamicAnimation, Pose>> poses);
	}

	public static final BindModifier KEEP_CHILD_LOCROT = (entitypatch, baseLayerPose, result, livingMotion, wholeEntry, priority, joint, poses) -> {
		Pose currentPose = poses.get(priority).getSecond();
		JointTransform lowestTransform = baseLayerPose.orElseEmpty(joint.getName());
		JointTransform currentTransform = currentPose.orElseEmpty(joint.getName());
		result.getJointTransformData().getOrDefault(joint.getName(), JointTransform.empty()).translation().y = lowestTransform.translation().y;

		OpenMatrix4f lowestMatrix = lowestTransform.toMatrix();
		OpenMatrix4f currentMatrix = currentTransform.toMatrix();
		OpenMatrix4f.invert(currentMatrix, MATRIX_HOLDER_1);
		OpenMatrix4f currentToLowest = OpenMatrix4f.mul(MATRIX_HOLDER_1, lowestMatrix, MATRIX_HOLDER_2);

		for (Joint subJoint : joint.getSubJoints()) {
			if (wholeEntry.isMasked(livingMotion, subJoint.getName())) {
				OpenMatrix4f lowestLocalTransform = OpenMatrix4f.mul(joint.getLocalTransform(), lowestMatrix, MATRIX_HOLDER_3);
				OpenMatrix4f currentLocalTransform = OpenMatrix4f.mul(joint.getLocalTransform(), currentMatrix, MATRIX_HOLDER_4);
				OpenMatrix4f childTransform = OpenMatrix4f.mul(subJoint.getLocalTransform(), result.orElseEmpty(subJoint.getName()).toMatrix(), MATRIX_HOLDER_5);
				OpenMatrix4f lowestFinal = OpenMatrix4f.mul(lowestLocalTransform, childTransform, MATRIX_HOLDER_6);
				OpenMatrix4f currentFinal = OpenMatrix4f.mul(currentLocalTransform, childTransform, MATRIX_HOLDER_7);
				VEC_HOLDER.set((currentFinal.m30 - lowestFinal.m30) * 0.5F, currentFinal.m31 - lowestFinal.m31, currentFinal.m32 - lowestFinal.m32);
				JointTransform jt = result.getJointTransformData().getOrDefault(subJoint.getName(), JointTransform.empty());
				jt.parent(JointTransform.getTranslation(VEC_HOLDER), OpenMatrix4f::mul);
				jt.jointLocal(JointTransform.fromMatrixNoScale(currentToLowest), OpenMatrix4f::mul);
			}
		}
	};

	public static JointMask of(String jointName, BindModifier bindModifier) {
		return new JointMask(jointName, bindModifier);
	}

	public static JointMask of(String jointName) {
		return new JointMask(jointName, null);
	}

	private final String jointName;
	private final BindModifier bindModifier;

	private JointMask(String jointName, BindModifier bindModifier) {
		this.jointName = jointName;
		this.bindModifier = bindModifier;
	}

	@OnlyIn(Dist.CLIENT)
	public static class JointMaskSet {
		private final Map<String, BindModifier> masks;

		private JointMaskSet(Map<String, BindModifier> masks) {
			this.masks = Collections.unmodifiableMap(masks);
		}

		public boolean contains(String name) {
			return this.masks.containsKey(name);
		}

		public BindModifier getBindModifier(String jointName) {
			return this.masks.getOrDefault(jointName, null);
		}

		public static JointMaskSet of(JointMask... masks) {
			Map<String, BindModifier> map = Maps.newHashMap();
			for (JointMask jointMask : masks) {
				map.put(jointMask.jointName, jointMask.bindModifier);
			}
			return new JointMaskSet(map);
		}

		public static JointMaskSet of(Set<JointMask> jointMasks) {
			Map<String, BindModifier> map = Maps.newHashMap();
			for (JointMask jointMask : jointMasks) {
				map.put(jointMask.jointName, jointMask.bindModifier);
			}
			return new JointMaskSet(map);
		}
	}
}