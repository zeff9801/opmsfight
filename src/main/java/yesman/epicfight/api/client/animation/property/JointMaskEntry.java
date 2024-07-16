package yesman.epicfight.api.client.animation.property;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.client.animation.property.JointMask.JointMaskSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class JointMaskEntry {
	public static final JointMaskSet BIPED_UPPER_JOINTS_WITH_ROOT = JointMaskSet.of(
			JointMask.of("Root", JointMask.KEEP_CHILD_LOCROT), JointMask.of("Torso"),
			JointMask.of("Chest"), JointMask.of("Head"),
			JointMask.of("Shoulder_R"), JointMask.of("Arm_R"),
			JointMask.of("Hand_R"), JointMask.of("Elbow_R"),
			JointMask.of("Tool_R"), JointMask.of("Shoulder_L"),
			JointMask.of("Arm_L"), JointMask.of("Hand_L"),
			JointMask.of("Elbow_L"), JointMask.of("Tool_L")
	);

	public static final JointMaskSet ALL = JointMaskSet.of(
			JointMask.of("Root"), JointMask.of("Thigh_R"),
			JointMask.of("Leg_R"), JointMask.of("Knee_R"),
			JointMask.of("Thigh_L"), JointMask.of("Leg_L"),
			JointMask.of("Knee_L"), JointMask.of("Torso"),
			JointMask.of("Chest"), JointMask.of("Head"),
			JointMask.of("Shoulder_R"), JointMask.of("Arm_R"),
			JointMask.of("Hand_R"), JointMask.of("Elbow_R"),
			JointMask.of("Tool_R"), JointMask.of("Shoulder_L"),
			JointMask.of("Arm_L"), JointMask.of("Hand_L"),
			JointMask.of("Elbow_L"), JointMask.of("Tool_L")
	);

	public static final JointMaskEntry BASIC_ATTACK_MASK = JointMaskEntry.builder().defaultMask(JointMaskEntry.BIPED_UPPER_JOINTS_WITH_ROOT).create();

	private final Map<LivingMotion, JointMaskSet> masks = Maps.newHashMap();
	private final JointMaskSet defaultMask;

	public JointMaskEntry(JointMaskSet defaultMask, List<Pair<LivingMotion, JointMaskSet>> masks) {
		this.defaultMask = defaultMask;

		for (Pair<LivingMotion, JointMaskSet> mask : masks) {
			this.masks.put(mask.getLeft(), mask.getRight());
		}
	}

	public JointMaskSet getMask(LivingMotion livingmotion) {
		return this.masks.getOrDefault(livingmotion, this.defaultMask);
	}

	public boolean isMasked(LivingMotion livingmotion, String jointName) {
		return !this.masks.getOrDefault(livingmotion, this.defaultMask).contains(jointName);
	}

	public Set<Map.Entry<LivingMotion, JointMaskSet>> getEntries() {
		return this.masks.entrySet();
	}

	public JointMaskSet getDefaultMask() {
		return this.defaultMask;
	}

	public boolean isValid() {
		return this.defaultMask != null;
	}

	public static JointMaskEntry.Builder builder() {
		return new JointMaskEntry.Builder();
	}

	@OnlyIn(Dist.CLIENT)
	public static class Builder {
		private final List<Pair<LivingMotion, JointMaskSet>> masks = Lists.newArrayList();
		private JointMaskSet defaultMask = null;

		public JointMaskEntry.Builder mask(LivingMotion motion, JointMaskSet masks) {
			this.masks.add(Pair.of(motion, masks));
			return this;
		}

		public JointMaskEntry.Builder defaultMask(JointMaskSet masks) {
			this.defaultMask = masks;
			return this;
		}

		public JointMaskEntry create() {
			return new JointMaskEntry(this.defaultMask, this.masks);
		}
	}
}